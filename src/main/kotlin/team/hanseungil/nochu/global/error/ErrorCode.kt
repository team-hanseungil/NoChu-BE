package team.hanseungil.nochu.global.error

enum class ErrorCode(
    val message: String,
    val status: Int,
) {
    // Common
    INVALID_INPUT_VALUE("잘못된 입력값입니다.", 400),
    
    // Member
    MEMBER_NOT_FOUND("해당하는 회원을 찾을 수 없습니다.", 404),
    MEMBER_NICKNAME_ALREADY_EXISTS("이미 존재하는 사용자 닉네임입니다.", 409),
    
    // Emotion
    EMOTION_NOT_FOUND("감정 정보를 찾을 수 없습니다.", 404),
    EMOTION_FACE_NOT_DETECTED("사진 업로드는 얼굴 사진만 가능합니다.", 400),
    
    // Music
    MUSIC_NOT_FOUND("음악 정보를 찾을 수 없습니다.", 404),
    
    // Playlist
    PLAYLIST_NOT_FOUND("플레이리스트를 찾을 수 없습니다.", 404),

    // File
    FILE_EMPTY("파일이 비어 있습니다.", 400),
    FILE_NOT_FOUND("파일을 찾을 수 없습니다.", 404),
    FILE_EXTENSION_NOT_FOUND("파일 확장자를 찾을 수 없습니다.", 400),
    FILE_EXTENSION_NOT_ALLOWED("허용되지 않은 파일 확장자입니다.", 400),

    // External API
    EXTERNAL_API_ERROR("외부 API 호출에 실패했습니다.", 500),
    EXTERNAL_API_TIMEOUT("외부 API 요청 시간이 초과되었습니다.", 504),
    EXTERNAL_API_BAD_REQUEST("외부 API 요청이 잘못되었습니다.", 400),

    // S3
    S3_UPLOAD_FAILED("파일 업로드에 실패했습니다.", 500),

}