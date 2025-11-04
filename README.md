# Oliver Emergency

자동 비상 신고 시스템 - 화재 감지 시 AI 음성으로 자동 전화를 발신하는 Ktor 서버

## 프로젝트 개요

Oliver Emergency는 [올리버 프로젝트](https://github.com/wait)의 화재 감지 시스템과 연동하여 비상 상황 발생 시 자동으로 소방서나 관련 기관에 전화를 걸어 음성으로 신고하는 시스템입니다. 

ElevenLabs의 TTS(Text-to-Speech) 기술을 활용하여 자연스러운 한국어 음성을 생성하고, Vonage API를 통해 자동 전화 발신을 수행합니다.

> 제10회 전국 동아리 소프트웨어 경진대회 출품작

## 프로젝트 팀원

- [Sungju Cho](https://github.com/iamfiro) - 대시보드 서비스 디자인, AI 화재 신고 기능 개발
- [Taegyeom Lee](https://github.com/kingcat47) - 올리버 대시보드 개발
- [Yuchan Han](https://github.com/h053698) - 대시보드 API 서버 개발
- [Wonyeong Kwak](https://github.com/i-dont-know-this-user-id) - 올리버 로봇 임베디드

### 환경 변수

다음 환경 변수를 설정해야 합니다:

```bash
ELEVENLABS_KEY=your_elevenlabs_api_key
VONAGE_KEY=your_vonage_api_key
VONAGE_SECRET=your_vonage_api_secret
VONAGE_APP_ID=your_vonage_application_id
PHONE_NUMBER=your_vonage_phone_number  # E.164 형식 (예: 821012345678)
BASE_URL=https://your-domain.com
```

## API Endpoint

### POST /call

비상 전화를 발신합니다.

**Request Body:**
```json
{
  "phoneNumber": "01012345678",
  "location": "서울시 강남구 테헤란로 123",
  "detectionTime": "2024-01-01T12:00:00+09:00"
}
```

**Response:**
```json
{
  "success": true,
  "message": "통화가 시작되었습니다.",
  "data": {
    "callUuid": "call-uuid-here",
    "audioUrl": "https://your-domain.com/audio/tts_1234567890.mp3"
  }
}
```

전화 수신자에게 다음과 같은 Elevenlabs에서 생성된 TTS가 재생됩니다.

`AI 화재 신고입니다. 화재가 발생했습니다. 위치는 00 입니다. 화재 감지 시간은 00시 00분 00초 입니다. 즉시 출동 바랍니다. 다시 한번 말씀 해드리겠습니다. 화재가 발생했습니다. 위치는 00 입니다. 화재 감지 시간은 00시 00분 00초 입니다.`