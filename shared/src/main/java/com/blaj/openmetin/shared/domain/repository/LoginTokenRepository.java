package com.blaj.openmetin.shared.domain.repository;

import com.blaj.openmetin.shared.domain.entity.LoginToken;

public interface LoginTokenRepository {

  void saveLoginToken(long loginKey, LoginToken loginToken);

  void saveLoginKey(long accountId, long loginKey);

  LoginToken getLoginToken(long loginKey);

  Long getAttempts(long accountId);

  Long getLoginKey(long accountId);

  boolean loginKeyExists(long accountId);

  void deleteLoginKey(long accountId);

  void deleteAttempts(long accountId);
}
