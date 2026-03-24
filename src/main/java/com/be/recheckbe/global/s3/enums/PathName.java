package com.be.recheckbe.global.s3.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PathName {
    RECEIPT("receipt");

    private final String path;
}