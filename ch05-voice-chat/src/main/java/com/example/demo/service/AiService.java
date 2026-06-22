package com.example.demo.service;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.content.Media;
import org.springframework.ai.openai.OpenAiAudioSpeechModel;
import org.springframework.ai.openai.OpenAiAudioSpeechOptions;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.ai.openai.OpenAiAudioTranscriptionOptions;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.openai.api.OpenAiApi.ChatCompletionRequest;
import org.springframework.ai.openai.api.OpenAiApi.ChatCompletionRequest.AudioParameters;
import org.springframework.ai.openai.api.OpenAiAudioApi.SpeechRequest;
import org.springframework.ai.openai.api.OpenAiAudioApi.SpeechRequest.AudioResponseFormat;
import org.springframework.ai.openai.audio.speech.SpeechPrompt;
import org.springframework.ai.openai.audio.speech.SpeechResponse;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiService {

  private final ChatClient chatClient;
  private final OpenAiAudioTranscriptionModel openAiAudioTranscriptionModel; //STT
  private final OpenAiAudioSpeechModel openAiAudioSpeechModel; //TTS

  // ##### 메소드 #####
  public String stt(byte[] bytes) { //매개변수: 음성 데이터 배열
    // 음성 데이터(byte[])->ByteArrayResource로 매핑
    Resource audioResource = new ByteArrayResource(bytes);

    // 모델 옵션 설정
    OpenAiAudioTranscriptionOptions options = OpenAiAudioTranscriptionOptions.builder()
        .model("whisper-1") //모델명
        .language("ko") // 입력 음성 언어의 종류 설정, 출력 언어에도 영향을 미침
        .build();

    // 프롬프트 생성
    AudioTranscriptionPrompt prompt = new AudioTranscriptionPrompt(audioResource, options);

    // STT 모델을 호출하고 응답으로 텍스트 받기
    AudioTranscriptionResponse response = openAiAudioTranscriptionModel.call(prompt);
    String text = response.getResult().getOutput();

    return text;
  }

  public byte[] tts(String text) {
    // 모델 옵션 설정
    OpenAiAudioSpeechOptions options = OpenAiAudioSpeechOptions.builder()
        .model("gpt-4o-mini-tts") //모델 지정
        .voice(SpeechRequest.Voice.ALLOY) //enum 중 하나 선택
        .responseFormat(SpeechRequest.AudioResponseFormat.MP3) //출력할 오디오 형식, enum 중 하나 선택
        .speed(1.0f) //음성 합성 속도, 기본값 1
        .build();

    // 프롬프트 생성
    SpeechPrompt prompt = new SpeechPrompt(text, options);

    // 모델을 호출하고 바이너리 음성데이터(byte[]) 응답받기
    SpeechResponse response = openAiAudioSpeechModel.call(prompt);
    byte[] bytes = response.getResult().getOutput();

    return bytes;
  }

  //question의 답을 LLM으로 요청해 <텍스트, 음성>으로 반환
  public Map<String, String> chatText(String question) {
    // LLM로 요청하고, 텍스트 응답 얻기
    String textAnswer = chatClient.prompt()
        .system("50자 이내로 한국어로 답변해주세요.")
        .user(question)
        .call()
        .content();

    // TTS 모델로 요청하고 응답으로 받은 음성 데이터를 base64 문자열로 변환
    byte[] audio = tts(textAnswer);
    String base64Audio = Base64.getEncoder().encodeToString(audio);

    // 텍스트 답변과 음성 답변을 Map에 저장
    Map<String, String> response = new HashMap<>();
    response.put("text", textAnswer);
    response.put("audio", base64Audio);

    return response;
  }

  //input: text -> output: speach streaming
  public Flux<byte[]> ttsFlux(String text) {
    // 모델 옵션 설정
    OpenAiAudioSpeechOptions options = OpenAiAudioSpeechOptions.builder()
            .model("gpt-4o-mini-tts")
            .voice(SpeechRequest.Voice.ALLOY)
            .responseFormat(AudioResponseFormat.MP3)
            .speed(1.0f)
            .build();

    // 프롬프트 생성
    SpeechPrompt prompt = new SpeechPrompt(text, options);

    // 모델로 요청하고 응답받기
    Flux<SpeechResponse> response = openAiAudioSpeechModel.stream(prompt);
    Flux<byte[]> flux = response.map(speechResponse -> speechResponse.getResult().getOutput());
    return flux;
  }

  //input: speach->output: speach streaming
  public Flux<byte[]> chatVoiceSttLlmTts(byte[] audioBytes) {
    // STT를 이용해서 음성 질문을 텍스트 질문으로 변환
    String textQuestion = stt(audioBytes);

    // 텍스트 질문으로 LLM에 요청하고, 텍스트 응답 얻기
    String textAnswer = chatClient.prompt()
        .system("50자 이내로 답변해주세요.")
        .user(textQuestion)
        .call()
        .content();

    // TTS를 이용해서 비동기 음성 데이터 얻기
    Flux<byte[]> flux = ttsFlux(textAnswer);
    return flux;
  }

  //input: text and speach -> output: text and speach
  public byte[] chatVoiceOneModel(byte[] audioBytes, String mimeType) throws Exception { //사용자 음성의 오디오 MIME 타입
    // 음성 데이터를 Resource로 생성
    Resource resource = new ByteArrayResource(audioBytes);

    // 사용자 메시지 생성
    UserMessage userMessage = UserMessage.builder()
        // 빈문자열이라도 제공해야함
        .text("제공되는 음성에 맞는 자연스러운 대화로 이어주세요.") //text 누락 시 에러
        .media(new Media(MimeType.valueOf(mimeType), resource)) //사용자의 음성 질문
        .build();

    // 모델 옵션 설정
    ChatOptions chatOptions = OpenAiChatOptions.builder()
        .model(OpenAiApi.ChatModel.GPT_4_O_MINI_AUDIO_PREVIEW) //모델명 ENUM
        .outputModalities(List.of("text", "audio")) //텍스트, 오디오 답변 전부
        .outputAudio(new AudioParameters(
            ChatCompletionRequest.AudioParameters.Voice.ALLOY,
            ChatCompletionRequest.AudioParameters.AudioResponseFormat.MP3))
        .build();

    // gpt-4o-mini-audio 모델은 스트림을 지원하지 않기 때문에 동기 방식 사용
    // 모델로 요청하고 응답 받기
    ChatResponse response = chatClient.prompt()
        .system("50자 이내로 답변해주세요.")
        .messages(userMessage)
        .options(chatOptions)
        .call()
        .chatResponse();
    
    // AI 메시지 얻기
    AssistantMessage assistantMessage = response.getResult().getOutput();
    
    // 텍스트 답변 얻기
    String textAnswer = assistantMessage.getText();
    log.info("텍스트 응답: {}", textAnswer);

    // 오디오 답변 얻기
    byte[] audioAnswer = assistantMessage.getMedia().get(0).getDataAsByteArray();

    return audioAnswer;
  }  
}
