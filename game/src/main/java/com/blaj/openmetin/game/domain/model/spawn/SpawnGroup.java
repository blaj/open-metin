package com.blaj.openmetin.game.domain.model.spawn;

import java.util.List;

public record SpawnGroup(long id, String name, long leaderId, List<Long> membersIds) {}
