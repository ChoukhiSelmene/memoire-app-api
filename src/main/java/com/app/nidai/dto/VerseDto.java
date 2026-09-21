package com.app.nidai.dto;

public record VerseDto(
    Integer chapterNumber,
    Integer verseNumber,
    Boolean isEndOfChapter
) {}
