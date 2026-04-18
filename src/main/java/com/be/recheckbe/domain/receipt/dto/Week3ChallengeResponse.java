package com.be.recheckbe.domain.receipt.dto;

import lombok.Getter;

@Getter
public class Week3ChallengeResponse {

  private final String matchup;
  private final String win;
  private final String lose;
  private final boolean isDraw;
  private final int year1Total;
  private final int year2Total;

  private Week3ChallengeResponse(
      String matchup, String win, String lose, boolean isDraw, int year1Total, int year2Total) {
    this.matchup = matchup;
    this.win = win;
    this.lose = lose;
    this.isDraw = isDraw;
    this.year1Total = year1Total;
    this.year2Total = year2Total;
  }

  public static Week3ChallengeResponse of(
      String year1Label, int year1Total, String year2Label, int year2Total) {
    String matchup = year1Label + "vs" + year2Label;
    if (year1Total > year2Total) {
      return new Week3ChallengeResponse(
          matchup, year1Label, year2Label, false, year1Total, year2Total);
    } else if (year2Total > year1Total) {
      return new Week3ChallengeResponse(
          matchup, year2Label, year1Label, false, year1Total, year2Total);
    } else {
      return new Week3ChallengeResponse(matchup, null, null, true, year1Total, year2Total);
    }
  }
}
