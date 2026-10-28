import test from 'node:test';
import assert from 'node:assert/strict';
import {
  generateKnockoutTournament,
  generateRoundRobinTournament,
  pairDoublesRandomly,
  calculateStandings,
  updateMatchScore,
} from './tournamentEngine.js';

function createPlayers(count) {
  return Array.from({ length: count }, (_, i) => ({
    id: `p_${i + 1}`,
    name: `Player ${i + 1}`,
  }));
}

test('Knockout Bracket with 2 players', () => {
  const players = createPlayers(2);
  const t = generateKnockoutTournament('Tour 2', players);
  assert.equal(t.rounds.length, 1);
  assert.equal(t.rounds[0].matches.length, 1);
  assert.equal(t.rounds[0].matches[0].isBye, false);
});

test('Knockout Bracket with 3 players (1 Bye -> 2 rounds)', () => {
  const players = createPlayers(3);
  const t = generateKnockoutTournament('Tour 3', players);
  assert.equal(t.rounds.length, 2);
  assert.equal(t.rounds[0].matches.length, 2);
  const byes = t.rounds[0].matches.filter((m) => m.isBye);
  assert.equal(byes.length, 1);
  assert.ok(byes[0].winner);
  // Propagated to round 1
  const finalMatch = t.rounds[1].matches[0];
  assert.ok(finalMatch.participant1 || finalMatch.participant2);
});

test('Knockout Bracket with 5 players (3 Byes -> 3 rounds)', () => {
  const players = createPlayers(5);
  const t = generateKnockoutTournament('Tour 5', players);
  assert.equal(t.rounds.length, 3);
  assert.equal(t.rounds[0].matches.length, 4);
  const byes = t.rounds[0].matches.filter((m) => m.isBye);
  assert.equal(byes.length, 3);
  assert.equal(t.rounds[0].matches.filter((m) => !m.isBye).length, 1);
});

test('Knockout Bracket with 7 players (1 Bye -> 3 rounds)', () => {
  const players = createPlayers(7);
  const t = generateKnockoutTournament('Tour 7', players);
  assert.equal(t.rounds.length, 3);
  assert.equal(t.rounds[0].matches.length, 4);
  const byes = t.rounds[0].matches.filter((m) => m.isBye);
  assert.equal(byes.length, 1);
  assert.equal(t.rounds[0].matches.filter((m) => !m.isBye).length, 3);
});

test('Knockout Bracket with 8 players (0 Byes -> 3 rounds)', () => {
  const players = createPlayers(8);
  const t = generateKnockoutTournament('Tour 8', players);
  assert.equal(t.rounds.length, 3);
  assert.equal(t.rounds[0].matches.length, 4);
  const byes = t.rounds[0].matches.filter((m) => m.isBye);
  assert.equal(byes.length, 0);
  assert.equal(t.rounds[0].matches.filter((m) => !m.isBye).length, 4);
});

test('Knockout Bracket with 13 players (3 Byes -> 4 rounds)', () => {
  const players = createPlayers(13);
  const t = generateKnockoutTournament('Tour 13', players);
  assert.equal(t.rounds.length, 4);
  assert.equal(t.rounds[0].matches.length, 8);
  const byes = t.rounds[0].matches.filter((m) => m.isBye);
  assert.equal(byes.length, 3);
  assert.equal(t.rounds[0].matches.filter((m) => !m.isBye).length, 5);
});

test('Knockout Bracket with 16 players (0 Byes -> 4 rounds)', () => {
  const players = createPlayers(16);
  const t = generateKnockoutTournament('Tour 16', players);
  assert.equal(t.rounds.length, 4);
  assert.equal(t.rounds[0].matches.length, 8);
  const byes = t.rounds[0].matches.filter((m) => m.isBye);
  assert.equal(byes.length, 0);
  assert.equal(t.rounds[0].matches.filter((m) => !m.isBye).length, 8);
});

test('Knockout Bracket with 25 players (7 Byes -> 5 rounds)', () => {
  const players = createPlayers(25);
  const t = generateKnockoutTournament('Tour 25', players);
  assert.equal(t.rounds.length, 5);
  assert.equal(t.rounds[0].matches.length, 16);
  const byes = t.rounds[0].matches.filter((m) => m.isBye);
  assert.equal(byes.length, 7);
  assert.equal(t.rounds[0].matches.filter((m) => !m.isBye).length, 9);
});

test('Doubles random pairing with odd number of players', () => {
  const names = ['Ali', 'Omar', 'Hassan', 'Ziad', 'Karim']; // 5 players
  const { pairs, leftover } = pairDoublesRandomly(names);
  assert.equal(pairs.length, 2);
  assert.ok(leftover);
  assert.ok(names.includes(leftover));

  // Resolved when even
  const evenNames = [...names, 'Tarek'];
  const { pairs: evenPairs, leftover: evenLeftover } = pairDoublesRandomly(evenNames);
  assert.equal(evenPairs.length, 3);
  assert.equal(evenLeftover, null);
});

test('Round Robin and Standings Calculation', () => {
  const players = createPlayers(4);
  const rules = { bestOf: 3, pointsToWin: 11, pointsMargin: 2 };
  let t = generateRoundRobinTournament('League', players, rules);

  assert.equal(t.rounds.length, 3); // 4 players -> 3 rounds
  assert.equal(t.rounds[0].matches.length, 2);

  // Score match 1
  const m = t.rounds[0].matches[0];
  t = updateMatchScore(t, m.id, [
    { player1Score: 11, player2Score: 8 },
    { player1Score: 11, player2Score: 9 },
  ]);

  const standings = calculateStandings(t);
  assert.equal(standings.length, 4);
  const top = standings[0];
  assert.equal(top.participant.id, m.participant1.id);
  assert.equal(top.points, 2);
  assert.equal(top.won, 1);
  assert.equal(top.setsWon, 2);
  assert.equal(top.setsLost, 0);
});
