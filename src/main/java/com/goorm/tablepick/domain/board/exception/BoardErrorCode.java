package com.goorm.tablepick.domain.board.exception;

public enum BoardErrorCode {
    NOT_FOUND("게시글을 찾을 수 없습니다."),
    NO_PERMISSION("해당 게시글에 대한 권한이 없습니다.");

    private final String message;

    BoardErrorCode(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
