package com.blaj.openmetin.game.domain.repository;

import com.blaj.openmetin.game.domain.entity.BannedWord;
import com.blaj.openmetin.shared.domain.repository.ArchiveEntityRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BannedWordRepository extends ArchiveEntityRepository<BannedWord> {

  boolean existsByWord(String word);
}
