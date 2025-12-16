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
    
    // Music
    MUSIC_NOT_FOUND("음악 정보를 찾을 수 없습니다.", 404),
    
    // Playlist
    PLAYLIST_NOT_FOUND("플레이리스트를 찾을 수 없습니다.", 404),

    // File
    FILE_EMPTY("파일이 비어 있습니다.", 400),
    FILE_NOT_FOUND("파일을 찾을 수 없습니다.", 404),
    FILE_EXTENSION_NOT_FOUND("파일 확장자를 찾을 수 없습니다.", 400),
    FILE_EXTENSION_NOT_ALLOWED("허용되지 않은 파일 확장자입니다.", 400),

}