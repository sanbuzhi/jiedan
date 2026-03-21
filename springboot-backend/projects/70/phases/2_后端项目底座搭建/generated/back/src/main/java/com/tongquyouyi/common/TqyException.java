package com.tongquyouyi.common;

import lombok.Getter;

/**
 * 业务异常类
 *
 * @author tongquyouyi
 * @since 2024-01-01
 */
@Getter
public class TqyException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final Integer code;

    public TqyException(String msg) {
        super(msg);
        this.code = 500;
    }

    public TqyException(Integer code, String msg) {
        super(msg);
        this.code = code;
    }

    public TqyException(ErrorCode errorCode) {
        super(errorCode.getMsg());
        this.code = errorCode.getCode();
    }

}