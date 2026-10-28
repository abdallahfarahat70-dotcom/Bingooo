import React from 'react';
import { Tournament } from '../types';
import { Language, t } from '../utils/strings';
import { X, Trash2, Trophy, Users } from 'lucide-react';

interface Props {
  tournaments: Tournament[];
  lang: Language;
  onSelect: (tournament: Tournament) => void;
  onDelete: (id: string) => void;
  onClose: () => void;
}

export const HistoryModal: React.FC<Props> = ({
  tournaments,
  lang,
  onSelect,
  onDelete,
  onClose,
}) => {
  const formatDate = (ts: number) => {
    try {
      const d = new Date(ts);
      // Format without year to comply strictly with year omission rule
      return new Intl.DateTimeFormat(lang === 'ar' ? 'ar-EG' : 'en-US', {
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      }).format(d);
    } catch {
      return '';
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/75 backdrop-blur-sm animate-in fade-in duration-200">
      <div className="bg-slate-900 border border-slate-700/70 w-full max-w-md rounded-3xl p-6 shadow-2xl flex flex-col max-h-[85vh]">
        <div className="flex items-center justify-between pb-4 border-b border-slate-800">
          <h3 className="text-lg font-bold text-slate-100">{t('history_title', lang)}</h3>
          <button
            onClick={onClose}
            className="p-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-400 hover:text-white transition"
          >
            <X size={18} />
          </button>
        </div>

        <div className="py-4 overflow-y-auto space-y-3 flex-1">
          {tournaments.length === 0 ? (
            <div className="text-center py-12 text-slate-500 font-medium text-sm">
              {t('history_empty', lang)}
            </div>
          ) : (
            tournaments.map((item) => (
              <div
                key={item.id}
                onClick={() => {
                  onSelect(item);
                  onClose();
                }}
                className="p-3.5 rounded-2xl bg-slate-800/60 hover:bg-slate-800 border border-slate-700/50 hover:border-brand-orange/50 transition cursor-pointer flex items-center justify-between gap-3 group"
              >
                <div className="flex-1 min-w-0">
                  <h4 className="font-bold text-slate-200 text-sm truncate group-hover:text-brand-orange transition">
                    {item.name}
                  </h4>
                  <div className="flex items-center gap-2 text-xs text-slate-400 mt-1">
                    <span>{item.format === 'knockout' ? t('knockout', lang) : t('round_robin', lang)}</span>
                    <span>•</span>
                    <span className="flex items-center gap-1">
                      <Users size={12} />
                      {item.participants.length}
                    </span>
                    <span>•</span>
                    <span>{formatDate(item.createdAt)}</span>
                  </div>
                  {item.winner && (
                    <div className="flex items-center gap-1 text-xs text-amber-400 font-semibold mt-1">
                      <Trophy size={12} />
                      <span className="truncate">
                        {item.winner.partnerName
                          ? `${item.winner.name} & ${item.winner.partnerName}`
                          : item.winner.name}
                      </span>
                    </div>
                  )}
                </div>

                <button
                  onClick={(e) => {
                    e.stopPropagation();
                    if (confirm(lang === 'ar' ? 'هل تريد حذف هذه الدورة؟' : 'Delete this tournament?')) {
                      onDelete(item.id);
                    }
                  }}
                  className="p-2 rounded-xl text-slate-500 hover:text-rose-400 hover:bg-rose-500/10 transition"
                >
                  <Trash2 size={16} />
                </button>
              </div>
            ))
          )}
        </div>
      </div>
    </div>
  );
};
