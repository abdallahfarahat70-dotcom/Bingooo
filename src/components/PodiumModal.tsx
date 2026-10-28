import React, { useEffect } from 'react';
import { Tournament } from '../types';
import { Language, t } from '../utils/strings';
import confetti from 'canvas-confetti';
import { Trophy, Share2, X } from 'lucide-react';

interface Props {
  tournament: Tournament;
  lang: Language;
  onShare: () => void;
  onClose: () => void;
}

export const PodiumModal: React.FC<Props> = ({
  tournament,
  lang,
  onShare,
  onClose,
}) => {
  const winner = tournament.winner;
  const runnerUp = tournament.runnerUp;
  const thirdPlace = tournament.thirdPlace;

  useEffect(() => {
    // Fire confetti celebration
    const end = Date.now() + 2500;
    const colors = ['#FF5E00', '#00ADB5', '#FFD700', '#10B981', '#FFFFFF'];

    (function frame() {
      confetti({
        particleCount: 4,
        angle: 60,
        spread: 55,
        origin: { x: 0 },
        colors,
      });
      confetti({
        particleCount: 4,
        angle: 120,
        spread: 55,
        origin: { x: 1 },
        colors,
      });

      if (Date.now() < end) {
        requestAnimationFrame(frame);
      }
    })();
  }, []);

  const getDisplayName = (p: typeof winner) => {
    if (!p) return '—';
    return p.partnerName ? `${p.name} & ${p.partnerName}` : p.name;
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-md animate-in fade-in duration-300">
      <div className="bg-slate-900 border-2 border-amber-500/40 w-full max-w-md rounded-3xl p-6 shadow-2xl relative overflow-hidden flex flex-col items-center text-center">
        {/* Close Button */}
        <button
          onClick={onClose}
          className="absolute top-4 right-4 p-2 rounded-xl bg-slate-800 text-slate-400 hover:text-white transition"
        >
          <X size={18} />
        </button>

        {/* Trophy Glow Icon */}
        <div className="w-20 h-20 rounded-full bg-gradient-to-tr from-amber-500 to-yellow-300 p-0.5 shadow-xl shadow-amber-500/30 mb-3 flex items-center justify-center">
          <div className="w-full h-full rounded-full bg-slate-900 flex items-center justify-center">
            <Trophy size={40} className="text-amber-400 animate-bounce" />
          </div>
        </div>

        <h2 className="text-2xl font-black text-slate-100">{t('champion_title', lang)}</h2>
        <p className="text-sm text-slate-400 mt-1 font-medium">{tournament.name}</p>

        {/* Podium Steps */}
        <div className="w-full flex items-end justify-center gap-2 mt-8 mb-6 h-56 px-2">
          {/* 2nd Place (Silver) */}
          <div className="flex-1 flex flex-col items-center">
            <span className="text-2xl mb-1">🥈</span>
            <span className="text-xs font-bold text-slate-300 line-clamp-2 px-1 mb-2">
              {getDisplayName(runnerUp)}
            </span>
            <div className="w-full h-28 bg-gradient-to-b from-slate-400/30 to-slate-400/10 border-t-2 border-slate-300 rounded-t-2xl flex items-center justify-center font-black text-3xl text-slate-300 shadow-inner">
              2
            </div>
          </div>

          {/* 1st Place (Gold) */}
          <div className="flex-[1.2] flex flex-col items-center">
            <span className="text-3xl mb-1">🥇</span>
            <span className="text-sm font-black text-amber-300 line-clamp-2 px-1 mb-2">
              {getDisplayName(winner)}
            </span>
            <div className="w-full h-40 bg-gradient-to-b from-amber-500/40 to-amber-500/10 border-t-2 border-amber-400 rounded-t-2xl flex items-center justify-center font-black text-5xl text-amber-400 shadow-lg shadow-amber-500/20">
              1
            </div>
          </div>

          {/* 3rd Place (Bronze) */}
          <div className="flex-1 flex flex-col items-center">
            <span className="text-2xl mb-1">🥉</span>
            <span className="text-xs font-bold text-slate-300 line-clamp-2 px-1 mb-2">
              {getDisplayName(thirdPlace)}
            </span>
            <div className="w-full h-20 bg-gradient-to-b from-amber-700/30 to-amber-700/10 border-t-2 border-amber-600 rounded-t-2xl flex items-center justify-center font-black text-2xl text-amber-600 shadow-inner">
              3
            </div>
          </div>
        </div>

        {/* Buttons */}
        <div className="w-full flex gap-3">
          <button
            onClick={onClose}
            className="flex-1 py-3 rounded-xl border border-slate-700 hover:bg-slate-800 text-slate-300 font-bold text-sm transition"
          >
            {t('close', lang)}
          </button>
          <button
            onClick={onShare}
            className="flex-[1.2] py-3 rounded-xl bg-brand-orange hover:bg-brand-orangeDark text-white font-bold text-sm flex items-center justify-center gap-2 shadow-lg shadow-brand-orange/25 transition active:scale-[0.98]"
          >
            <Share2 size={16} />
            <span>{t('share_btn', lang)}</span>
          </button>
        </div>
      </div>
    </div>
  );
};
