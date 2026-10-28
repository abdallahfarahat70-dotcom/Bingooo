import { Match, MatchRule, Participant, Round, SetScore, Standing, Tournament, TournamentFormat, TournamentType } from '../types';

export function nextPowerOfTwo(value: number): number {
  if (value <= 2) return 2;
  let power = 2;
  while (power < value) {
    power *= 2;
  }
  return power;
}

export function isSetFinished(set: SetScore, rule: MatchRule): boolean {
  const maxScore = Math.max(set.player1Score, set.player2Score);
  const diff = Math.abs(set.player1Score - set.player2Score);
  return maxScore >= rule.pointsToWin && diff >= rule.pointsMargin;
}

export function setWinner(set: SetScore, rule: MatchRule): 1 | 2 | null {
  if (!isSetFinished(set, rule)) return null;
  return set.player1Score > set.player2Score ? 1 : 2;
}

export function playerSetsWon(match: Match, rule: MatchRule, playerNum: 1 | 2): number {
  return match.sets.filter((s) => setWinner(s, rule) === playerNum).length;
}

export function generateKnockoutTournament(
  name: string,
  participants: Participant[],
  rules: MatchRule = { bestOf: 3, pointsToWin: 11, pointsMargin: 2 },
  type: TournamentType = 'singles',
  id: string = crypto.randomUUID ? crypto.randomUUID() : 'tour_' + Date.now()
): Tournament {
  if (participants.length < 2) {
    throw new Error('At least 2 participants required');
  }

  const count = participants.length;
  const bracketSize = nextPowerOfTwo(count); // 2, 4, 8, 16, 32...
  const totalRounds = Math.round(Math.log2(bracketSize));
  const numByes = bracketSize - count;
  const round0MatchCount = bracketSize / 2;

  // Evenly spaced bye distribution
  const byeMatchIndices = new Set<number>();
  if (numByes > 0) {
    for (let i = 0; i < numByes; i++) {
      byeMatchIndices.add(Math.floor((i * round0MatchCount) / numByes));
    }
  }

  // Pre-generate match IDs for wiring up parents to children
  const matchIdsByRound: string[][] = [];
  for (let r = 0; r < totalRounds; r++) {
    const matchCount = bracketSize / Math.pow(2, r + 1);
    matchIdsByRound.push(
      Array.from({ length: matchCount }, (_, m) => `match_r${r}_m${m}_${Date.now()}_${Math.random().toString(36).substring(2, 7)}`)
    );
  }

  // Build Round 0
  let pIdx = 0;
  const round0Matches: Match[] = [];

  for (let m = 0; m < round0MatchCount; m++) {
    const matchId = matchIdsByRound[0][m];
    const nextMatchId = totalRounds > 1 ? matchIdsByRound[1][Math.floor(m / 2)] : null;
    const nextMatchSlot = ((m % 2) + 1) as 1 | 2;

    if (byeMatchIndices.has(m)) {
      const p1 = participants[pIdx++];
      round0Matches.push({
        id: matchId,
        roundIndex: 0,
        matchIndexInRound: m,
        participant1: p1,
        participant2: null,
        sets: [],
        winner: p1,
        isBye: true,
        nextMatchId,
        nextMatchSlot,
        isCompleted: true,
      });
    } else {
      const p1 = participants[pIdx++];
      const p2 = participants[pIdx++];
      round0Matches.push({
        id: matchId,
        roundIndex: 0,
        matchIndexInRound: m,
        participant1: p1,
        participant2: p2,
        sets: [],
        winner: null,
        isBye: false,
        nextMatchId,
        nextMatchSlot,
        isCompleted: false,
      });
    }
  }

  // Build subsequent rounds
  const allRounds: Round[] = [
    {
      roundIndex: 0,
      titleKey: getRoundTitleKey(0, totalRounds),
      roundNumber: 1,
      matches: round0Matches,
    },
  ];

  for (let r = 1; r < totalRounds; r++) {
    const matchCount = bracketSize / Math.pow(2, r + 1);
    const matches: Match[] = [];
    const nextRoundIds = r + 1 < totalRounds ? matchIdsByRound[r + 1] : null;

    for (let m = 0; m < matchCount; m++) {
      const matchId = matchIdsByRound[r][m];
      const nextMatchId = nextRoundIds ? nextRoundIds[Math.floor(m / 2)] : null;
      const nextMatchSlot = ((m % 2) + 1) as 1 | 2;

      matches.push({
        id: matchId,
        roundIndex: r,
        matchIndexInRound: m,
        participant1: null,
        participant2: null,
        sets: [],
        winner: null,
        isBye: false,
        nextMatchId,
        nextMatchSlot,
        isCompleted: false,
      });
    }

    allRounds.push({
      roundIndex: r,
      titleKey: getRoundTitleKey(r, totalRounds),
      roundNumber: r + 1,
      matches,
    });
  }

  let tournament: Tournament = {
    id,
    name,
    type,
    format: 'knockout',
    rules,
    participants,
    rounds: allRounds,
    createdAt: Date.now(),
    isFinished: false,
    winner: null,
    runnerUp: null,
    thirdPlace: null,
  };

  // Propagate Round 0 byes to Round 1
  for (const match of round0Matches) {
    if (match.isBye && match.winner && match.nextMatchId) {
      tournament = advanceWinner(tournament, match.winner, match.nextMatchId, match.nextMatchSlot || 1);
    }
  }

  return tournament;
}

export function generateRoundRobinTournament(
  name: string,
  participants: Participant[],
  rules: MatchRule = { bestOf: 3, pointsToWin: 11, pointsMargin: 2 },
  type: TournamentType = 'singles',
  id: string = crypto.randomUUID ? crypto.randomUUID() : 'tour_' + Date.now()
): Tournament {
  if (participants.length < 2) {
    throw new Error('At least 2 participants required');
  }

  const pool: (Participant | null)[] = [...participants];
  if (pool.length % 2 !== 0) {
    pool.push(null); // Ghost for bye
  }

  const n = pool.length;
  const totalRounds = n - 1;
  const matchesPerRound = n / 2;
  const rounds: Round[] = [];
  const rotatingList = [...pool];

  for (let r = 0; r < totalRounds; r++) {
    const matches: Match[] = [];
    for (let m = 0; m < matchesPerRound; m++) {
      const p1 = rotatingList[m];
      const p2 = rotatingList[n - 1 - m];

      if (p1 !== null && p2 !== null) {
        matches.push({
          id: `rr_r${r}_m${m}_${Date.now()}_${Math.random().toString(36).substring(2, 6)}`,
          roundIndex: r,
          matchIndexInRound: matches.length,
          participant1: p1,
          participant2: p2,
          sets: [],
          winner: null,
          isBye: false,
          isCompleted: false,
        });
      }
    }

    rounds.push({
      roundIndex: r,
      titleKey: 'round_n',
      roundNumber: r + 1,
      matches,
    });

    // Polygon rotation: fix 0, rotate 1..n-1
    const last = rotatingList.pop()!;
    rotatingList.splice(1, 0, last);
  }

  return {
    id,
    name,
    type,
    format: 'round_robin',
    rules,
    participants,
    rounds,
    createdAt: Date.now(),
    isFinished: false,
    winner: null,
    runnerUp: null,
    thirdPlace: null,
  };
}

function advanceWinner(tournament: Tournament, winner: Participant, nextMatchId: string, slot: 1 | 2): Tournament {
  const updatedRounds = tournament.rounds.map((round) => ({
    ...round,
    matches: round.matches.map((match) => {
      if (match.id === nextMatchId) {
        return slot === 1 ? { ...match, participant1: winner } : { ...match, participant2: winner };
      }
      return match;
    }),
  }));
  return { ...tournament, rounds: updatedRounds };
}

export function updateMatchScore(tournament: Tournament, matchId: string, sets: SetScore[]): Tournament {
  let updatedTournament = { ...tournament };
  let completedWinner: Participant | null = null;
  let nextMatchId: string | null = null;
  let nextMatchSlot: 1 | 2 = 1;
  let isFinalMatch = false;

  const setsNeeded = Math.floor(tournament.rules.bestOf / 2) + 1;

  const updatedRounds = tournament.rounds.map((round) => ({
    ...round,
    matches: round.matches.map((match) => {
      if (match.id === matchId) {
        const p1Sets = sets.filter((s) => setWinner(s, tournament.rules) === 1).length;
        const p2Sets = sets.filter((s) => setWinner(s, tournament.rules) === 2).length;

        let winner: Participant | null = null;
        if (p1Sets >= setsNeeded) winner = match.participant1;
        else if (p2Sets >= setsNeeded) winner = match.participant2;

        const isCompleted = winner !== null;
        if (isCompleted && winner) {
          completedWinner = winner;
          nextMatchId = match.nextMatchId || null;
          nextMatchSlot = match.nextMatchSlot || 1;
          if (match.roundIndex === tournament.rounds.length - 1) {
            isFinalMatch = true;
          }
        }

        return { ...match, sets, winner, isCompleted };
      }
      return match;
    }),
  }));

  updatedTournament = { ...updatedTournament, rounds: updatedRounds };

  if (completedWinner && nextMatchId) {
    updatedTournament = advanceWinner(updatedTournament, completedWinner, nextMatchId, nextMatchSlot);
  }

  // Knockout final conclusion
  if (isFinalMatch && completedWinner) {
    const finalRound = updatedTournament.rounds[updatedTournament.rounds.length - 1];
    const finalMatch = finalRound.matches[0];
    const winnerObj = completedWinner as Participant;
    const runnerUp =
      finalMatch.participant1?.id === winnerObj.id ? finalMatch.participant2 : finalMatch.participant1;

    let thirdPlace: Participant | null = null;
    if (updatedTournament.rounds.length >= 2) {
      const semiRound = updatedTournament.rounds[updatedTournament.rounds.length - 2];
      const semiLosers = semiRound.matches
        .map((m) => (m.winner?.id === m.participant1?.id ? m.participant2 : m.participant1))
        .filter(Boolean) as Participant[];
      thirdPlace = semiLosers[0] || null;
    }

    updatedTournament = {
      ...updatedTournament,
      isFinished: true,
      winner: completedWinner,
      runnerUp,
      thirdPlace,
    };
  }

  // Round robin completion check
  if (tournament.format === 'round_robin') {
    const allMatches = updatedTournament.rounds.flatMap((r) => r.matches);
    const allDone = allMatches.length > 0 && allMatches.every((m) => m.isCompleted);
    if (allDone) {
      const standings = calculateStandings(updatedTournament);
      updatedTournament = {
        ...updatedTournament,
        isFinished: true,
        winner: standings[0]?.participant || null,
        runnerUp: standings[1]?.participant || null,
        thirdPlace: standings[2]?.participant || null,
      };
    }
  }

  return updatedTournament;
}

export function resetMatchScore(tournament: Tournament, matchId: string): Tournament {
  const targetMatch = tournament.rounds.flatMap((r) => r.matches).find((m) => m.id === matchId);
  if (!targetMatch) return tournament;

  const updatedRounds = tournament.rounds.map((round) => ({
    ...round,
    matches: round.matches.map((match) => {
      if (match.id === matchId) {
        return { ...match, sets: [], winner: null, isCompleted: false };
      } else if (targetMatch.nextMatchId && match.id === targetMatch.nextMatchId) {
        return targetMatch.nextMatchSlot === 1
          ? { ...match, participant1: null, winner: null, isCompleted: false }
          : { ...match, participant2: null, winner: null, isCompleted: false };
      }
      return match;
    }),
  }));

  return {
    ...tournament,
    rounds: updatedRounds,
    isFinished: false,
    winner: null,
    runnerUp: null,
    thirdPlace: null,
  };
}

export function calculateStandings(tournament: Tournament): Standing[] {
  const map = new Map<string, Standing>();
  for (const p of tournament.participants) {
    map.set(p.id, {
      participant: p,
      played: 0,
      won: 0,
      lost: 0,
      setsWon: 0,
      setsLost: 0,
      setDiff: 0,
      pointsWon: 0,
      pointsLost: 0,
      pointDiff: 0,
      points: 0,
    });
  }

  const allMatches = tournament.rounds.flatMap((r) => r.matches);
  for (const m of allMatches) {
    if (!m.isCompleted || m.isBye || !m.participant1 || !m.participant2) continue;

    const s1 = map.get(m.participant1.id);
    const s2 = map.get(m.participant2.id);
    if (!s1 || !s2) continue;

    const p1Sets = playerSetsWon(m, tournament.rules, 1);
    const p2Sets = playerSetsWon(m, tournament.rules, 2);

    let p1Pts = 0;
    let p2Pts = 0;
    for (const s of m.sets) {
      p1Pts += s.player1Score;
      p2Pts += s.player2Score;
    }

    const p1Won = m.winner?.id === m.participant1.id;
    const p2Won = m.winner?.id === m.participant2.id;

    s1.played += 1;
    s1.won += p1Won ? 1 : 0;
    s1.lost += p2Won ? 1 : 0;
    s1.setsWon += p1Sets;
    s1.setsLost += p2Sets;
    s1.setDiff = s1.setsWon - s1.setsLost;
    s1.pointsWon += p1Pts;
    s1.pointsLost += p2Pts;
    s1.pointDiff = s1.pointsWon - s1.pointsLost;
    s1.points += p1Won ? 2 : 0;

    s2.played += 1;
    s2.won += p2Won ? 1 : 0;
    s2.lost += p1Won ? 1 : 0;
    s2.setsWon += p2Sets;
    s2.setsLost += p1Sets;
    s2.setDiff = s2.setsWon - s2.setsLost;
    s2.pointsWon += p2Pts;
    s2.pointsLost += p1Pts;
    s2.pointDiff = s2.pointsWon - s2.pointsLost;
    s2.points += p2Won ? 2 : 0;
  }

  return Array.from(map.values()).sort((a, b) => {
    if (b.points !== a.points) return b.points - a.points;
    if (b.setDiff !== a.setDiff) return b.setDiff - a.setDiff;
    if (b.pointDiff !== a.pointDiff) return b.pointDiff - a.pointDiff;
    return b.setsWon - a.setsWon;
  });
}

export function pairDoublesRandomly(names: string[]): { pairs: Participant[]; leftover: string | null } {
  const shuffled = [...names].sort(() => Math.random() - 0.5);
  const pairs: Participant[] = [];
  const pairCount = Math.floor(shuffled.length / 2);

  for (let i = 0; i < pairCount; i++) {
    pairs.push({
      id: `pair_${i}_${Date.now()}_${Math.random().toString(36).substring(2, 6)}`,
      name: shuffled[i * 2],
      partnerName: shuffled[i * 2 + 1],
    });
  }

  const leftover = shuffled.length % 2 !== 0 ? shuffled[shuffled.length - 1] : null;
  return { pairs, leftover };
}

export function getRoundTitleKey(roundIndex: number, totalRounds: number): string {
  const fromEnd = totalRounds - 1 - roundIndex;
  switch (fromEnd) {
    case 0:
      return 'final';
    case 1:
      return 'semi';
    case 2:
      return 'quarter';
    case 3:
      return 'round_of_16';
    case 4:
      return 'round_of_32';
    default:
      return 'preliminary';
  }
}
