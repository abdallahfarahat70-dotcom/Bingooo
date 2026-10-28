export type TournamentType = 'singles' | 'doubles';
export type TournamentFormat = 'knockout' | 'round_robin';
export type DoublesEntryMode = 'manual_pairs' | 'random_draw';

export interface MatchRule {
  bestOf: number;       // 1, 3, or 5
  pointsToWin: number;  // Default 11
  pointsMargin: number; // Default 2
}

export interface Participant {
  id: string;
  name: string;
  partnerName?: string;
}

export interface SetScore {
  player1Score: number;
  player2Score: number;
}

export interface Match {
  id: string;
  roundIndex: number;
  matchIndexInRound: number;
  participant1: Participant | null;
  participant2: Participant | null;
  sets: SetScore[];
  winner: Participant | null;
  isBye: boolean;
  nextMatchId?: string | null;
  nextMatchSlot?: 1 | 2;
  isCompleted: boolean;
}

export interface Round {
  roundIndex: number;
  titleKey: string;
  roundNumber: number;
  matches: Match[];
}

export interface Standing {
  participant: Participant;
  played: number;
  won: number;
  lost: number;
  setsWon: number;
  setsLost: number;
  setDiff: number;
  pointsWon: number;
  pointsLost: number;
  pointDiff: number;
  points: number;
}

export interface Tournament {
  id: string;
  name: string;
  type: TournamentType;
  format: TournamentFormat;
  rules: MatchRule;
  participants: Participant[];
  rounds: Round[];
  createdAt: number;
  isFinished: boolean;
  winner: Participant | null;
  runnerUp: Participant | null;
  thirdPlace: Participant | null;
}
