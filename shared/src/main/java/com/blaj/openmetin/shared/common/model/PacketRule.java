package com.blaj.openmetin.shared.common.model;

import com.blaj.openmetin.shared.common.enums.Phase;
import com.blaj.openmetin.shared.common.enums.SequenceBehavior;
import java.util.EnumSet;

public record PacketRule(
    Byte header, SequenceBehavior sequenceBehavior, EnumSet<Phase> phaseEnumSet) {}
