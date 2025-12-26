package com.blaj.openmetin.shared.domain.repository;

import com.blaj.openmetin.shared.domain.entity.LoginToken;
import org.joou.UInteger;

public interface LoginTokenRepository {

  void saveLoginToken(UInteger loginKey, LoginToken loginToken);

  void saveLoginKey(long accountId, UInteger loginKey);

  LoginToken getLoginToken(UInteger loginKey);

  Long getAttempts(long accountId);

  Long getLoginKey(long accountId);

  boolean loginKeyExists(long accountId);

  void deleteLoginKey(long accountId);

  void deleteAttempts(long accountId);
}
