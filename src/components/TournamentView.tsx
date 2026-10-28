import React, { useState } from 'react';
import { Match, SetScore, Tournament } from '../types';
import { Language, t } from '../utils/strings';
import { calculateStandings, playerSetsWon, resetMatchScore, updateMatchScore } from '../utils/tournamentEngine';
import { BingBongLogo } from './BingBongLogo';
import { ScoreModal } from './ScoreModal';
import { PodiumModal } from './PodiumModal';
import {
  ArrowLeft,
  ArrowRight,
  Share2,
  Trophy,
  History,
  Languages,
  Sun,
  Moon,
  CheckCircle2,
  PlayCircle,
  Clock,
  RotateCcw,
} from 'lucide-react';

interface Props {
  tournament: Tournament;
  lang: Language;
  onLanguageToggle: () => void;
  isDark: boolean;
  onThemeToggle: () => void;
  onOpenHistory: () => void;
  onNewTournament: () => void;
  onUpdateTournament: (updated: Tournament) => void;
}

export const TournamentView: React.FC<Props> = ({
  tournament,
  lang,
  onLanguageToggle,
  isDark,
  onThemeToggle,
  onOpenHistory,
  onNewTournament,
  onUpdateTournament,
}) => {
  const [selectedMatch, setSelectedMatch] = useState<Match | null>(null);
  const [showPodium, setShowPodium] = useState<boolean>(tournament.isFinished);
  const [activeTab, setActiveTab] = useState<'matches' | 'standings'>('matches');

  const standings = calculateStandings(tournament);

  const handleSaveScore = (sets: SetScore[]) => {
    if (!selectedMatch) return;
    const updated = updateMatchScore(tournament, selectedMatch.id, sets);
    onUpdateTournament(updated);
    if (updated.isFinished && !tournament.isFinished) {
      setShowPodium(true);
    }
  };

  const handleResetScore = () => {
    if (!selectedMatch) return;
    const updated = resetMatchScore(tournament, selectedMatch.id);
    onUpdateTournament(updated);
  };

  const generateReportText = () => {
    let dateStr = '';
    try {
      dateStr = new Intl.DateTimeFormat(lang === 'ar' ? 'ar-EG' : 'en-US', {
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      }).format(new Date(tournament.createdAt));
    } catch {}

    let text = `🏓 ${tournament.name}\n`;
    if (dateStr) text += `📅 ${dateStr}\n`;
    text += `⚡ ${tournament.format === 'knockout' ? t('knockout', lang) : t('round_robin', lang)} (${tournament.type === 'singles' ? t('singles', lang) : t('doubles', lang)})\n\n`;

    if (tournament.winner) {
      text += `🏆 ${t('champion_title', lang)}\n`;
      text += `🥇 1st: ${tournament.winner.partnerName ? `${tournament.winner.name} & ${tournament.winner.partnerName}` : tournament.winner.name}\n`;
      if (tournament.runnerUp) {
        text += `🥈 2nd: ${tournament.runnerUp.partnerName ? `${tournament.runnerUp.name} & ${tournament.runnerUp.partnerName}` : tournament.runnerUp.name}\n`;
      }
      if (tournament.thirdPlace) {
        text += `🥉 3rd: ${tournament.thirdPlace.partnerName ? `${tournament.thirdPlace.name} & ${tournament.thirdPlace.partnerName}` : tournament.thirdPlace.name}\n`;
      }
      text += `\n`;
    }

    text += `📋 ${t('rounds_tab', lang)}:\n`;
    tournament.rounds.forEach((round) => {
      const roundTitle =
        round.titleKey === 'round_n'
          ? t('round_n', lang, round.roundNumber)
          : (t as any)(round.titleKey, lang);
      text += `\n▶ ${roundTitle}:\n`;
      round.matches.forEach((m) => {
        const p1 = m.participant1?.partnerName
          ? `${m.participant1.name} & ${m.participant1.partnerName}`
          : m.participant1?.name || '—';
        const p2 = m.isBye
          ? t('status_bye', lang)
          : m.participant2?.partnerName
          ? `${m.participant2.name} & ${m.participant2.partnerName}`
          : m.participant2?.name || '—';
        const p1Sets = playerSetsWon(m, tournament.rules, 1);
        const p2Sets = playerSetsWon(m, tournament.rules, 2);

        if (m.isCompleted) {
          const star1 = m.winner?.id === m.participant1?.id ? '★ ' : '';
          const star2 = m.winner?.id === m.participant2?.id ? ' ★' : '';
          text += `  • ${star1}${p1} (${p1Sets}) vs (${p2Sets}) ${p2}${star2}\n`;
        } else {
          text += `  • ${p1} vs ${p2}\n`;
        }
      });
    });

    text += `\n-- Bing bong --`;
    return text;
  };

  const handleShare = () => {
    const text = generateReportText();
    if (typeof navigator !== 'undefined' && navigator.share) {
      navigator.share({ title: tournament.name, text }).catch(() => {});
    } else {
      navigator.clipboard.writeText(text);
      alert(t('copied_toast', lang));
    }
  };

  const BackIcon = lang === 'ar' ? ArrowRight : ArrowLeft;

  return (
    <div className="min-h-screen flex flex-col">
      {/* Top Bar */}
      <header className="bg-slate-900/90 backdrop-blur-md border-b border-slate-800 sticky top-0 z-30 px-4 py-3 flex items-center justify-between">
        <div className="flex items-center gap-3">
          <button
            onClick={onNewTournament}
            className="p-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-300 transition"
            title={t('back', lang)}
          >
            <BackIcon size={18} />
          </button>
          <BingBongLogo size={36} showText={false} />
          <div className="leading-tight">
            <h2 className="font-bold text-sm text-slate-100 max-w-[160px] sm:max-w-xs truncate">
              {tournament.name}
            </h2>
            <span className="text-[11px] text-brand-orange font-semibold">
              {tournament.format === 'knockout' ? t('knockout', lang) : t('round_robin', lang)}
            </span>
          </div>
        </div>

        <div className="flex items-center gap-2">
          {tournament.isFinished && (
            <button
              onClick={() => setShowPodium(true)}
              className="p-2 rounded-xl bg-amber-500/20 hover:bg-amber-500/30 text-amber-400 border border-amber-500/40 transition"
              title={t('champion_title', lang)}
            >
              <Trophy size={18} />
            </button>
          )}

          <button
            onClick={handleShare}
            className="p-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-300 transition"
            title={t('share_btn', lang)}
          >
            <Share2 size={18} />
          </button>

          <button
            onClick={onOpenHistory}
            className="p-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-300 transition"
            title={t('history_title', lang)}
          >
            <History size={18} />
          </button>

          <button
            onClick={onLanguageToggle}
            className="px-2.5 py-1.5 rounded-xl bg-slate-800 hover:bg-slate-700 text-brand-orange font-bold text-xs transition"
          >
            {t('switch_lang', lang)}
          </button>

          <button
            onClick={onThemeToggle}
            className="p-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-300 transition"
          >
            {isDark ? <Sun size={18} /> : <Moon size={18} />}
          </button>
        </div>
      </header>

      {/* Tabs for Round Robin */}
      {tournament.format === 'round_robin' && (
        <div className="bg-slate-900 border-b border-slate-800 flex justify-center px-4">
          <div className="flex gap-2 p-1.5 max-w-md w-full">
            <button
              onClick={() => setActiveTab('matches')}
              className={`flex-1 py-2 rounded-xl text-xs font-bold transition ${
                activeTab === 'matches'
                  ? 'bg-brand-orange text-white shadow-md'
                  : 'text-slate-400 hover:text-slate-200'
              }`}
            >
              {t('rounds_tab', lang)}
            </button>
            <button
              onClick={() => setActiveTab('standings')}
              className={`flex-1 py-2 rounded-xl text-xs font-bold transition ${
                activeTab === 'standings'
                  ? 'bg-brand-orange text-white shadow-md'
                  : 'text-slate-400 hover:text-slate-200'
              }`}
            >
              {t('standings_tab', lang)}
            </button>
          </div>
        </div>
      )}

      {/* Main Content */}
      <main className="flex-1 overflow-x-auto p-4 sm:p-6">
        {tournament.format === 'knockout' ? (
          /* Knockout Visual Bracket Tree */
          <div className="flex gap-8 min-w-max pb-12 pt-2 items-start justify-center mx-auto">
            {tournament.rounds.map((round) => {
              const roundTitle =
                round.titleKey === 'final'
                  ? t('final', lang)
                  : round.titleKey === 'semi'
                  ? t('semi', lang)
                  : round.titleKey === 'quarter'
                  ? t('quarter', lang)
                  : round.titleKey === 'round_of_16'
                  ? t('round_of_16', lang)
                  : round.titleKey === 'round_of_32'
                  ? t('round_of_32', lang)
                  : t('preliminary', lang);

              const isFinal = round.titleKey === 'final';

              return (
                <div key={round.roundIndex} className="w-72 flex flex-col gap-4">
                  {/* Round Header */}
                  <div
                    className={`py-2.5 px-4 rounded-2xl border text-center font-bold text-sm shadow-sm flex items-center justify-center gap-2 ${
                      isFinal
                        ? 'bg-amber-500/15 border-amber-500/50 text-amber-300'
                        : 'bg-slate-800/80 border-slate-700/60 text-slate-200'
                    }`}
                  >
                    {isFinal && <Trophy size={16} className="text-amber-400" />}
                    <span>{roundTitle}</span>
                  </div>

                  {/* Matches list */}
                  <div className="flex flex-col gap-4 flex-1 justify-around">
                    {round.matches.map((match) => {
                      const p1 = match.participant1;
                      const p2 = match.participant2;
                      const hasPlayers = p1 && (p2 || match.isBye);
                      const p1Sets = playerSetsWon(match, tournament.rules, 1);
                      const p2Sets = playerSetsWon(match, tournament.rules, 2);

                      return (
                        <div
                          key={match.id}
                          onClick={() => {
                            if (hasPlayers && !match.isBye) {
                              setSelectedMatch(match);
                            }
                          }}
                          className={`rounded-2xl border p-3.5 shadow-md transition-all ${
                            match.isCompleted
                              ? 'bg-slate-800/90 border-emerald-500/60'
                              : hasPlayers && !match.isBye
                              ? 'bg-slate-800/70 border-brand-orange/40 hover:border-brand-orange cursor-pointer hover:scale-[1.01]'
                              : 'bg-slate-900/60 border-slate-800 opacity-60'
                          }`}
                        >
                          {/* Match Header / Sets preview */}
                          <div className="flex items-center justify-between text-[11px] mb-2.5">
                            <span
                              className={`px-2 py-0.5 rounded-md font-bold ${
                                match.isBye
                                  ? 'bg-brand-turquoise/20 text-brand-turquoise'
                                  : match.isCompleted
                                  ? 'bg-emerald-500/20 text-emerald-400'
                                  : hasPlayers
                                  ? 'bg-brand-orange/20 text-brand-orange'
                                  : 'bg-slate-800 text-slate-500'
                              }`}
                            >
                              {match.isBye
                                ? t('status_bye', lang)
                                : match.isCompleted
                                ? t('status_completed', lang)
                                : hasPlayers
                                ? t('status_in_progress', lang)
                                : t('status_upcoming', lang)}
                            </span>

                            {match.sets.length > 0 && (
                              <div className="flex gap-1 text-[10px] font-bold text-slate-400">
                                {match.sets.map((s, idx) => (
                                  <span key={idx}>
                                    {s.player1Score}-{s.player2Score}
                                  </span>
                                ))}
                              </div>
                            )}
                          </div>

                          {/* Player 1 Row */}
                          <div
                            className={`flex items-center justify-between p-2 rounded-xl transition ${
                              match.winner?.id === p1?.id && p1
                                ? 'bg-emerald-500/15 text-emerald-300 font-bold'
                                : 'text-slate-200'
                            }`}
                          >
                            <span className="text-xs truncate flex-1">
                              {p1 ? (p1.partnerName ? `${p1.name} & ${p1.partnerName}` : p1.name) : '—'}
                            </span>
                            <span
                              className={`w-6 h-6 rounded-lg flex items-center justify-center font-bold text-xs ${
                                match.winner?.id === p1?.id && p1
                                  ? 'bg-emerald-500 text-slate-900'
                                  : 'bg-slate-700/60 text-slate-300'
                              }`}
                            >
                              {p1Sets}
                            </span>
                          </div>

                          <div className="h-px bg-slate-700/40 my-1"></div>

                          {/* Player 2 Row (or Bye) */}
                          {match.isBye ? (
                            <div className="p-2 text-center text-xs text-brand-turquoise font-medium">
                              — {t('status_bye', lang)} —
                            </div>
                          ) : (
                            <div
                              className={`flex items-center justify-between p-2 rounded-xl transition ${
                                match.winner?.id === p2?.id && p2
                                  ? 'bg-emerald-500/15 text-emerald-300 font-bold'
                                  : 'text-slate-200'
                              }`}
                            >
                              <span className="text-xs truncate flex-1">
                                {p2 ? (p2.partnerName ? `${p2.name} & ${p2.partnerName}` : p2.name) : '—'}
                              </span>
                              <span
                                className={`w-6 h-6 rounded-lg flex items-center justify-center font-bold text-xs ${
                                  match.winner?.id === p2?.id && p2
                                  ? 'bg-emerald-500 text-slate-900'
                                  : 'bg-slate-700/60 text-slate-300'
                                }`}
                              >
                                {p2Sets}
                              </span>
                            </div>
                          )}
                        </div>
                      );
                    })}
                  </div>
                </div>
              );
            })}
          </div>
        ) : activeTab === 'matches' ? (
          /* Round Robin Matches */
          <div className="max-w-2xl mx-auto space-y-6">
            {tournament.rounds.map((round) => (
              <div
                key={round.roundIndex}
                className="bg-slate-800/40 border border-slate-700/50 rounded-2xl p-4 space-y-3"
              >
                <h4 className="font-bold text-sm text-brand-orange flex items-center gap-2">
                  <PlayCircle size={16} />
                  <span>{t('round_n', lang, round.roundNumber)}</span>
                </h4>

                <div className="space-y-2.5">
                  {round.matches.map((match) => {
                    const p1 = match.participant1;
                    const p2 = match.participant2;
                    const p1Sets = playerSetsWon(match, tournament.rules, 1);
                    const p2Sets = playerSetsWon(match, tournament.rules, 2);

                    return (
                      <div
                        key={match.id}
                        onClick={() => setSelectedMatch(match)}
                        className={`p-3 rounded-xl border flex items-center justify-between gap-4 cursor-pointer hover:border-brand-orange transition ${
                          match.isCompleted
                            ? 'bg-slate-800/90 border-emerald-500/40'
                            : 'bg-slate-800/50 border-slate-700/60'
                        }`}
                      >
                        <div className="flex-1 flex items-center justify-between">
                          <span
                            className={`text-xs truncate ${
                              match.winner?.id === p1?.id ? 'font-black text-emerald-400' : 'text-slate-200'
                            }`}
                          >
                            {p1?.partnerName ? `${p1.name} & ${p1.partnerName}` : p1?.name}
                          </span>
                          <span className="font-black text-sm px-2 text-slate-300">{p1Sets}</span>
                        </div>

                        <span className="text-slate-600 font-bold text-xs">:</span>

                        <div className="flex-1 flex items-center justify-between">
                          <span className="font-black text-sm px-2 text-slate-300">{p2Sets}</span>
                          <span
                            className={`text-xs truncate ${
                              match.winner?.id === p2?.id ? 'font-black text-emerald-400' : 'text-slate-200'
                            }`}
                          >
                            {p2?.partnerName ? `${p2.name} & ${p2.partnerName}` : p2?.name}
                          </span>
                        </div>
                      </div>
                    );
                  })}
                </div>
              </div>
            ))}
          </div>
        ) : (
          /* Round Robin Standings Table */
          <div className="max-w-2xl mx-auto bg-slate-800/60 border border-slate-700/60 rounded-2xl p-4 overflow-x-auto">
            <table className="w-full text-xs text-right">
              <thead>
                <tr className="border-b border-slate-700/60 text-slate-400 font-bold pb-2">
                  <th className="py-2 px-1 text-center w-8">{t('rank', lang)}</th>
                  <th className="py-2 px-2 text-right">{t('player_team', lang)}</th>
                  <th className="py-2 px-1 text-center w-8">{t('played', lang)}</th>
                  <th className="py-2 px-1 text-center w-8 text-emerald-400">{t('won', lang)}</th>
                  <th className="py-2 px-1 text-center w-8 text-rose-400">{t('lost', lang)}</th>
                  <th className="py-2 px-1 text-center w-14">{t('sets', lang)}</th>
                  <th className="py-2 px-1 text-center w-12 font-black text-brand-orange">
                    {t('points', lang)}
                  </th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-700/30 font-medium">
                {standings.map((s, idx) => {
                  const medal = idx === 0 ? '🥇' : idx === 1 ? '🥈' : idx === 2 ? '🥉' : idx + 1;
                  return (
                    <tr
                      key={s.participant.id}
                      className={
                        idx === 0 && tournament.isFinished
                          ? 'bg-emerald-500/10 font-bold text-slate-100'
                          : 'hover:bg-slate-700/20 text-slate-200'
                      }
                    >
                      <td className="py-2.5 px-1 text-center">{medal}</td>
                      <td className="py-2.5 px-2 truncate max-w-[150px]">
                        {s.participant.partnerName
                          ? `${s.participant.name} & ${s.participant.partnerName}`
                          : s.participant.name}
                      </td>
                      <td className="py-2.5 px-1 text-center text-slate-400">{s.played}</td>
                      <td className="py-2.5 px-1 text-center font-bold text-emerald-400">{s.won}</td>
                      <td className="py-2.5 px-1 text-center text-slate-400">{s.lost}</td>
                      <td className="py-2.5 px-1 text-center text-slate-300">
                        {s.setsWon}-{s.setsLost}
                      </td>
                      <td className="py-2.5 px-1 text-center font-black text-sm text-brand-orange">
                        {s.points}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </main>

      {/* Score Modal */}
      {selectedMatch && (
        <ScoreModal
          match={selectedMatch}
          rules={tournament.rules}
          lang={lang}
          onSave={handleSaveScore}
          onReset={handleResetScore}
          onClose={() => setSelectedMatch(null)}
        />
      )}

      {/* Podium Modal */}
      {showPodium && tournament.isFinished && (
        <PodiumModal
          tournament={tournament}
          lang={lang}
          onShare={handleShare}
          onClose={() => setShowPodium(false)}
        />
      )}
    </div>
  );
};
