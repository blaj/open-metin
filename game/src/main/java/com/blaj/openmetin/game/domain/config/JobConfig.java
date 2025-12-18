package com.blaj.openmetin.game.domain.config;

public record JobConfig(
    int ht,
    int st,
    int dx,
    int iq,
    int startHp,
    int startSp,
    int hpPerHt,
    int spPerIq,
    int hpPerLevel,
    int spPerLevel) {}
