package com.blaj.openmetin.game.domain.repository;

import com.blaj.openmetin.game.domain.entity.ServerStatus;
import java.util.List;

public interface ServerStatusRepository {

  void saveServerStatus(ServerStatus serverStatus);

  List<ServerStatus> getServerStatuses();
}
