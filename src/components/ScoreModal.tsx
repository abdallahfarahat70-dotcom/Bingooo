import React, { useState } from 'react';
import { Match, MatchRule, SetScore } from '../types';
import { Language, t } from '../utils/strings';
import { isSetFinished, setWinner } from '../utils/tournamentEngine';
import { X, Check, RotateCcw, Trophy, Plus, Minus } from 'lucide-react';

interface Props {
  match: Match;
  rules: MatchRule;
  lang: Language;
  onSave: (sets: SetScore[]) => void;
  onReset: () => void;
  onClose: () => void;
}

export const ScoreModal: React.FC<Props> = ({
  match,
  rules,
  lang,
  onSave,
  onReset,
  onClose,
}) => {
  const p1 = match.participant1;
  const p2 = match.participant2;

  const [sets, setSets] = useState<SetScore[]>(() => {
    const list = [...match.sets];
    while (list.length < rules.bestOf) {
      list.push({ player1Score: 0, player2Score: 0 });
    }
    return list;
  });

  const p1SetsWon = sets.filter((s) => setWinner(s, rules) === 1).length;
  const p2SetsWon = sets.filter((s) => setWinner(s, rules) === 2).length;
  const setsNeeded = Math.floor(rules.bestOf / 2) + 1;

  const matchWinner = p1SetsWon >= setsNeeded ? p1 : p2SetsWon >= setsNeeded ? p2 : null;

  const vibrate = () => {
    if (typeof navigator !== 'undefined' && navigator.vibrate) {
      navigator.vibrate(20);
    }
  };

  const updateSetScore = (setIndex: number, playerNum: 1 | 2, delta: number) => {
    vibrate();
    setSets((prev) =>
      prev.map((s, idx) => {
        if (idx !== setIndex) return s;
        if (playerNum === 1) {
          return { ...s, player1Score: Math.max(0, s.player1Score + delta) };
        } else {
          return { ...s, player2Score: Math.max(0, s.player2Score + delta) };
        }
      })
    );
  };

  const applyPreset = (setIndex: number, s1: number, s2: number) => {
    vibrate();
    setSets((prev) =>
      prev.map((s, idx) => (idx === setIndex ? { player1Score: s1, player2Score: s2 } : s))
    );
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/75 backdrop-blur-sm animate-in fade-in duration-200">
      <div className="bg-slate-900 border border-slate-700/70 w-full max-w-lg rounded-3xl shadow-2xl overflow-hidden flex flex-col max-h-[90vh]">
        {/* Header */}
        <div className="px-6 py-4 border-b border-slate-800 flex items-center justify-between">
          <div>
            <h3 className="text-lg font-bold text-slate-100">{t('score_match_title', lang)}</h3>
            <p className="text-xs text-slate-400 mt-0.5">
              {t('best_of', lang, rules.bestOf)} • {t('points_to_win', lang, rules.pointsToWin)}
            </p>
          </div>
          <button
            onClick={onClose}
            className="p-2 rounded-xl bg-slate-800/80 hover:bg-slate-700 text-slate-400 hover:text-slate-200 transition"
          >
            <X size={20} />
          </button>
        </div>

        {/* Content */}
        <div className="p-6 overflow-y-auto space-y-5">
          {/* Scoreboard Card */}
          <div className="bg-slate-800/80 rounded-2xl p-4 border border-slate-700/50">
            <div className="flex items-center justify-between gap-4">
              <div className="flex-1 text-center">
                <span className="block font-bold text-slate-200 text-sm line-clamp-2">
                  {p1 ? (p1.partnerName ? `${p1.name} & ${p1.partnerName}` : p1.name) : '—'}
                </span>
                <span className={`text-4xl font-black mt-1 block ${p1SetsWon >= setsNeeded ? 'text-emerald-400' : 'text-brand-orange'}`}>
                  {p1SetsWon}
                </span>
              </div>

              <div className="flex flex-col items-center px-2">
                <span className="text-xs font-bold text-slate-500 uppercase tracking-wider">VS</span>
                <span className="text-[11px] text-slate-400 font-medium">{t('sets', lang)}</span>
              </div>

              <div className="flex-1 text-center">
                <span className="block font-bold text-slate-200 text-sm line-clamp-2">
                  {p2 ? (p2.partnerName ? `${p2.name} & ${p2.partnerName}` : p2.name) : '—'}
                </span>
                <span className={`text-4xl font-black mt-1 block ${p2SetsWon >= setsNeeded ? 'text-emerald-400' : 'text-brand-orange'}`}>
                  {p2SetsWon}
                </span>
              </div>
            </div>

            {matchWinner && (
              <div className="mt-3 pt-3 border-t border-slate-700/50 flex items-center justify-center gap-2 text-emerald-400 text-sm font-bold bg-emerald-500/10 py-1.5 rounded-xl">
                <Trophy size={16} />
                <span>
                  {t('winner', lang)} {matchWinner.partnerName ? `${matchWinner.name} & ${matchWinner.partnerName}` : matchWinner.name}
                </span>
              </div>
            )}
          </div>

          {/* Sets list */}
          <div className="space-y-3">
            {sets.map((set, idx) => {
              const finished = isSetFinished(set, rules);
              const winnerNum = setWinner(set, rules);

              return (
                <div
                  key={idx}
                  className={`p-3.5 rounded-2xl border transition-all ${
                    finished
                      ? 'bg-slate-800/60 border-brand-orange/40'
                      : 'bg-slate-800/30 border-slate-800'
                  }`}
                >
                  <div className="flex items-center justify-between mb-2">
                    <span className="text-xs font-bold text-slate-300">
                      {t('set_n', lang, idx + 1)}
                    </span>
                    {finished && (
                      <span className="flex items-center gap-1 text-[11px] font-bold text-emerald-400">
                        <Check size={14} />
                        {winnerNum === 1
                          ? p1?.name
                          : p2?.name}
                      </span>
                    )}
                  </div>

                  {/* Steppers */}
                  <div className="flex items-center justify-between gap-4">
                    {/* P1 Stepper */}
                    <div className="flex items-center gap-2">
                      <button
                        onClick={() => updateSetScore(idx, 1, -1)}
                        className="w-10 h-10 rounded-xl bg-slate-700 hover:bg-slate-600 text-slate-200 flex items-center justify-center font-bold text-lg active:scale-95 transition"
                      >
                        <Minus size={18} />
                      </button>
                      <div
                        className={`w-14 h-10 rounded-xl border flex items-center justify-center font-black text-xl ${
                          winnerNum === 1
                            ? 'bg-emerald-500/20 border-emerald-500 text-emerald-400'
                            : 'bg-slate-800 border-slate-700 text-slate-100'
                        }`}
                      >
                        {set.player1Score}
                      </div>
                      <button
                        onClick={() => updateSetScore(idx, 1, 1)}
                        className="w-10 h-10 rounded-xl bg-brand-orange/20 hover:bg-brand-orange/30 text-brand-orange border border-brand-orange/40 flex items-center justify-center font-bold text-lg active:scale-95 transition"
                      >
                        <Plus size={18} />
                      </button>
                    </div>

                    <span className="font-bold text-slate-500">:</span>

                    {/* P2 Stepper */}
                    <div className="flex items-center gap-2">
                      <button
                        onClick={() => updateSetScore(idx, 2, -1)}
                        className="w-10 h-10 rounded-xl bg-slate-700 hover:bg-slate-600 text-slate-200 flex items-center justify-center font-bold text-lg active:scale-95 transition"
                      >
                        <Minus size={18} />
                      </button>
                      <div
                        className={`w-14 h-10 rounded-xl border flex items-center justify-center font-black text-xl ${
                          winnerNum === 2
                            ? 'bg-emerald-500/20 border-emerald-500 text-emerald-400'
                            : 'bg-slate-800 border-slate-700 text-slate-100'
                        }`}
                      >
                        {set.player2Score}
                      </div>
                      <button
                        onClick={() => updateSetScore(idx, 2, 1)}
                        className="w-10 h-10 rounded-xl bg-brand-orange/20 hover:bg-brand-orange/30 text-brand-orange border border-brand-orange/40 flex items-center justify-center font-bold text-lg active:scale-95 transition"
                      >
                        <Plus size={18} />
                      </button>
                    </div>
                  </div>

                  {/* Quick score presets */}
                  <div className="flex items-center gap-1.5 mt-2.5 overflow-x-auto pb-0.5">
                    {[
                      [11, 8],
                      [11, 9],
                      [12, 10],
                      [8, 11],
                      [9, 11],
                      [10, 12],
                    ].map(([s1, s2], pIdx) => (
                      <button
                        key={pIdx}
                        onClick={() => applyPreset(idx, s1, s2)}
                        className="text-[11px] font-semibold px-2 py-1 rounded-lg bg-slate-700/60 hover:bg-slate-600 text-slate-300 hover:text-white transition"
                      >
                        {s1}-{s2}
                      </button>
                    ))}
                  </div>
                </div>
              );
            })}
          </div>
        </div>

        {/* Footer */}
        <div className="p-4 border-t border-slate-800 bg-slate-900/90 flex items-center gap-3">
          {match.isCompleted && (
            <button
              onClick={() => {
                onReset();
                onClose();
              }}
              className="flex-1 py-3 px-4 rounded-xl border border-slate-700 hover:bg-slate-800 text-slate-300 font-bold text-sm flex items-center justify-center gap-2 transition"
            >
              <RotateCcw size={16} />
              <span>{t('undo_result', lang)}</span>
            </button>
          )}

          <button
            onClick={() => {
              const active = sets.filter((s) => s.player1Score > 0 || s.player2Score > 0);
              onSave(active.length > 0 ? active : sets.slice(0, 1));
              onClose();
            }}
            className="flex-1 py-3 px-4 rounded-xl bg-brand-orange hover:bg-brand-orangeDark text-white font-bold text-sm flex items-center justify-center gap-2 shadow-lg shadow-brand-orange/25 transition active:scale-[0.98]"
          >
            <Check size={18} />
            <span>{t('save_result', lang)}</span>
          </button>
        </div>
      </div>
    </div>
  );
};
