package com.blaj.openmetin.shared.infrastructure.network.sequencing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import com.blaj.openmetin.shared.common.model.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PacketSequencerServiceTest {

  private PacketSequencerService packetSequencerService;

  @Mock private Session session;

  @BeforeEach
  public void beforeEach() {
    packetSequencerService = new PacketSequencerService();
  }

  @Test
  public void givenCorrectByte_whenConsumeInbound_thenReturnsTrue() {
    // given
    given(session.getSequenceIndex()).willReturn(0);

    // when
    var result = packetSequencerService.consumeInbound((byte) 0, session);

    // then
    assertThat(result).isTrue();
    then(session).should().setSequenceIndex(1);
  }

  @Test
  public void givenIncorrectByte_whenConsumeInbound_thenReturnsFalse() {
    // given
    given(session.getSequenceIndex()).willReturn(0);

    // when
    var result = packetSequencerService.consumeInbound((byte) 5, session);

    // then
    assertThat(result).isFalse();
    then(session).should(times(0)).setSequenceIndex(1);
  }

  @Test
  public void givenSequenceAt255_whenConsumeInbound_thenWrapsToZero() {
    // given
    given(session.getSequenceIndex()).willReturn(255);

    // when
    var result = packetSequencerService.consumeInbound((byte) 255, session);

    // then
    assertThat(result).isTrue();
    then(session).should().setSequenceIndex(0);
  }

  @Test
  public void givenSequenceInMiddle_whenConsumeInbound_thenIncrementsCorrectly() {
    // given
    given(session.getSequenceIndex()).willReturn(42);

    // when
    var result = packetSequencerService.consumeInbound((byte) 42, session);

    // then
    assertThat(result).isTrue();
    then(session).should().setSequenceIndex(43);
  }

  @Test
  public void givenSequenceAtZero_whenNextOutbound_thenReturnsZeroAndIncrements() {
    // given
    given(session.getSequenceIndex()).willReturn(0);

    // when
    var result = packetSequencerService.nextOutbound(session);

    // then
    assertThat(result).isEqualTo((byte) 0);
    then(session).should().setSequenceIndex(1);
  }

  @Test
  public void givenSequenceAt255_whenNextOutbound_thenReturns255AndWrapsToZero() {
    // given
    given(session.getSequenceIndex()).willReturn(255);

    // when
    var result = packetSequencerService.nextOutbound(session);

    // then
    assertThat(result).isEqualTo((byte) 255);
    then(session).should().setSequenceIndex(0);
  }

  @Test
  public void givenSequenceInMiddle_whenNextOutbound_thenReturnsCorrectByteAndIncrements() {
    // given
    given(session.getSequenceIndex()).willReturn(100);

    // when
    var result = packetSequencerService.nextOutbound(session);

    // then
    assertThat(result).isEqualTo((byte) 100);
    then(session).should().setSequenceIndex(101);
  }

  @Test
  public void givenMultipleNextOutboundCalls_whenCalled_thenGeneratesSequence() {
    // given
    given(session.getSequenceIndex()).willReturn(0, 0, 1, 1, 2, 2);

    // when
    var result1 = packetSequencerService.nextOutbound(session);
    var result2 = packetSequencerService.nextOutbound(session);
    var result3 = packetSequencerService.nextOutbound(session);

    // then
    assertThat(result1).isEqualTo((byte) 0);
    assertThat(result2).isEqualTo((byte) 1);
    assertThat(result3).isEqualTo((byte) 2);
    then(session).should().setSequenceIndex(1);
    then(session).should().setSequenceIndex(2);
    then(session).should().setSequenceIndex(3);
  }

  @Test
  public void givenNonZeroIndex_whenReset_thenSetsIndexToZero() {
    // given

    // when
    packetSequencerService.reset(session);

    // then
    then(session).should().setSequenceIndex(0);
  }

  @Test
  public void givenResetCalled_whenNextOutbound_thenStartsFromZero() {
    // given
    packetSequencerService.reset(session);
    given(session.getSequenceIndex()).willReturn(0);

    // when
    var result = packetSequencerService.nextOutbound(session);

    // then
    assertThat(result).isEqualTo((byte) 0);
  }

  @Test
  public void givenSequenceWrappedAround_whenConsumeInbound_thenHandlesCorrectly() {
    // given
    given(session.getSequenceIndex()).willReturn(254, 254, 255, 255, 0, 0);

    // when
    var result1 = packetSequencerService.consumeInbound((byte) 254, session);
    var result2 = packetSequencerService.consumeInbound((byte) 255, session);
    var result3 = packetSequencerService.consumeInbound((byte) 0, session);

    // then
    assertThat(result1).isTrue();
    assertThat(result2).isTrue();
    assertThat(result3).isTrue();
  }

  @Test
  public void givenNegativeByte_whenConsumeInbound_thenHandlesCorrectly() {
    // given
    given(session.getSequenceIndex()).willReturn(200);

    // when
    var result = packetSequencerService.consumeInbound((byte) 200, session);

    // then
    assertThat(result).isTrue();
    then(session).should().setSequenceIndex(201);
  }

  @Test
  public void givenConsecutiveInboundAndOutbound_whenCalled_thenMaintainsSeparateSequences() {
    // given
    given(session.getSequenceIndex()).willReturn(0, 0, 1, 1, 2, 2);

    // when
    var outbound1 = packetSequencerService.nextOutbound(session);
    var inbound1 = packetSequencerService.consumeInbound((byte) 1, session);
    var outbound2 = packetSequencerService.nextOutbound(session);

    // then
    assertThat(outbound1).isEqualTo((byte) 0);
    assertThat(inbound1).isTrue();
    assertThat(outbound2).isEqualTo((byte) 2);
  }
}
