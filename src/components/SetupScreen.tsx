import React, { useState } from 'react';
import { DoublesEntryMode, MatchRule, Participant, Tournament, TournamentFormat, TournamentType } from '../types';
import { Language, t } from '../utils/strings';
import { generateKnockoutTournament, generateRoundRobinTournament, pairDoublesRandomly } from '../utils/tournamentEngine';
import { BingBongLogo } from './BingBongLogo';
import {
  Users,
  User,
  Swords,
  RefreshCw,
  Shuffle,
  Plus,
  X,
  FileText,
  Sparkles,
  AlertTriangle,
  Play,
  History,
  Languages,
  Sun,
  Moon,
} from 'lucide-react';

interface Props {
  lang: Language;
  onLanguageToggle: () => void;
  isDark: boolean;
  onThemeToggle: () => void;
  onOpenHistory: () => void;
  onStartTournament: (tournament: Tournament) => void;
}

export const SetupScreen: React.FC<Props> = ({
  lang,
  onLanguageToggle,
  isDark,
  onThemeToggle,
  onOpenHistory,
  onStartTournament,
}) => {
  const [tournamentType, setTournamentType] = useState<TournamentType>('singles');
  const [tournamentFormat, setTournamentFormat] = useState<TournamentFormat>('knockout');
  const [tournamentName, setTournamentName] = useState('');

  // Rules
  const [bestOf, setBestOf] = useState<number>(3);
  const [pointsToWin, setPointsToWin] = useState<number>(11);
  const [pointsMargin] = useState<number>(2);

  // Singles Players
  const [singlesPlayers, setSinglesPlayers] = useState<string[]>([]);
  const [currentInput, setCurrentInput] = useState('');

  // Doubles
  const [doublesMode, setDoublesMode] = useState<DoublesEntryMode>('random_draw');
  const [doublesIndividual, setDoublesIndividual] = useState<string[]>([]);
  const [manualTeams, setManualTeams] = useState<[string, string][]>([]);
  const [teamP1, setTeamP1] = useState('');
  const [teamP2, setTeamP2] = useState('');

  // Paste Modal
  const [showPasteModal, setShowPasteModal] = useState(false);
  const [pasteContent, setPasteContent] = useState('');

  const activePlayersList = tournamentType === 'singles' ? singlesPlayers : doublesIndividual;

  const handleAddPlayer = () => {
    const trimmed = currentInput.trim();
    if (!trimmed) return;
    if (tournamentType === 'singles') {
      if (!singlesPlayers.includes(trimmed)) {
        setSinglesPlayers([...singlesPlayers, trimmed]);
      }
    } else {
      if (!doublesIndividual.includes(trimmed)) {
        setDoublesIndividual([...doublesIndividual, trimmed]);
      }
    }
    setCurrentInput('');
  };

  const handleRemovePlayer = (index: number) => {
    if (tournamentType === 'singles') {
      setSinglesPlayers(singlesPlayers.filter((_, i) => i !== index));
    } else {
      setDoublesIndividual(doublesIndividual.filter((_, i) => i !== index));
    }
  };

  const handleShuffle = () => {
    if (tournamentType === 'singles') {
      setSinglesPlayers([...singlesPlayers].sort(() => Math.random() - 0.5));
    } else {
      setDoublesIndividual([...doublesIndividual].sort(() => Math.random() - 0.5));
    }
  };

  const handleAddSamplePlayers = () => {
    const samples = [
      'محمد صلاح',
      'عمر خالد',
      'أحمد علي',
      'كريم عادل',
      'يوسف هاني',
      'طارق فتحي',
      'سامي إبراهيم',
      'زياد وليد',
    ];
    if (tournamentType === 'singles') {
      setSinglesPlayers(samples);
    } else {
      setDoublesIndividual(samples);
    }
  };

  const handlePasteSubmit = () => {
    const names = pasteContent
      .split(/[\n,]/)
      .map((s) => s.trim())
      .filter(Boolean);

    if (tournamentType === 'singles') {
      const merged = Array.from(new Set([...singlesPlayers, ...names]));
      setSinglesPlayers(merged);
    } else {
      const merged = Array.from(new Set([...doublesIndividual, ...names]));
      setDoublesIndividual(merged);
    }
    setPasteContent('');
    setShowPasteModal(false);
  };

  const totalTeamsCount =
    tournamentType === 'singles'
      ? singlesPlayers.length
      : doublesMode === 'manual_pairs'
      ? manualTeams.length
      : Math.floor(doublesIndividual.length / 2);

  const canStart = totalTeamsCount >= 2;
  const isOddDoubles = tournamentType === 'doubles' && doublesMode === 'random_draw' && doublesIndividual.length % 2 !== 0;

  const handleStart = () => {
    const rules: MatchRule = { bestOf, pointsToWin, pointsMargin };
    const name = tournamentName.trim() || (lang === 'ar' ? 'دورة بينج بونج' : 'Ping Pong Tournament');

    let participants: Participant[] = [];
    if (tournamentType === 'singles') {
      participants = singlesPlayers.map((name, i) => ({
        id: `p_${i}_${Date.now()}`,
        name,
      }));
    } else {
      if (doublesMode === 'manual_pairs') {
        participants = manualTeams.map(([p1, p2], i) => ({
          id: `team_${i}_${Date.now()}`,
          name: p1,
          partnerName: p2,
        }));
      } else {
        const { pairs } = pairDoublesRandomly(doublesIndividual);
        participants = pairs;
      }
    }

    const tournament =
      tournamentFormat === 'knockout'
        ? generateKnockoutTournament(name, participants, rules, tournamentType)
        : generateRoundRobinTournament(name, participants, rules, tournamentType);

    onStartTournament(tournament);
  };

  return (
    <div className="max-w-2xl mx-auto px-4 py-6 space-y-6">
      {/* Header Bar */}
      <header className="flex items-center justify-between pb-4 border-b border-slate-800">
        <BingBongLogo showTagline taglineText={t('tagline', lang)} size={44} />

        <div className="flex items-center gap-2">
          <button
            onClick={onOpenHistory}
            className="p-2.5 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-300 transition"
            title={t('history_title', lang)}
          >
            <History size={18} />
          </button>

          <button
            onClick={onLanguageToggle}
            className="px-3 py-1.5 rounded-xl bg-slate-800 hover:bg-slate-700 text-brand-orange font-bold text-xs flex items-center gap-1.5 transition"
          >
            <Languages size={14} />
            <span>{t('switch_lang', lang)}</span>
          </button>

          <button
            onClick={onThemeToggle}
            className="p-2.5 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-300 transition"
            title={t('theme_toggle', lang)}
          >
            {isDark ? <Sun size={18} /> : <Moon size={18} />}
          </button>
        </div>
      </header>

      {/* 1. Singles vs Doubles Segmented Control */}
      <div className="bg-slate-800/80 p-1.5 rounded-2xl border border-slate-700/60 flex gap-2">
        <button
          onClick={() => setTournamentType('singles')}
          className={`flex-1 py-3 px-4 rounded-xl font-bold text-sm flex items-center justify-center gap-2 transition ${
            tournamentType === 'singles'
              ? 'bg-brand-orange text-white shadow-md'
              : 'text-slate-400 hover:text-slate-200'
          }`}
        >
          <User size={18} />
          <span>{t('singles', lang)}</span>
        </button>
        <button
          onClick={() => setTournamentType('doubles')}
          className={`flex-1 py-3 px-4 rounded-xl font-bold text-sm flex items-center justify-center gap-2 transition ${
            tournamentType === 'doubles'
              ? 'bg-brand-orange text-white shadow-md'
              : 'text-slate-400 hover:text-slate-200'
          }`}
        >
          <Users size={18} />
          <span>{t('doubles', lang)}</span>
        </button>
      </div>

      {/* 2. Knockout vs Round Robin */}
      <div className="bg-slate-800/80 p-1.5 rounded-2xl border border-slate-700/60 flex gap-2">
        <button
          onClick={() => setTournamentFormat('knockout')}
          className={`flex-1 py-3 px-4 rounded-xl font-bold text-sm flex items-center justify-center gap-2 transition ${
            tournamentFormat === 'knockout'
              ? 'bg-brand-orange text-white shadow-md'
              : 'text-slate-400 hover:text-slate-200'
          }`}
        >
          <Swords size={18} />
          <span>{t('knockout', lang)}</span>
        </button>
        <button
          onClick={() => setTournamentFormat('round_robin')}
          className={`flex-1 py-3 px-4 rounded-xl font-bold text-sm flex items-center justify-center gap-2 transition ${
            tournamentFormat === 'round_robin'
              ? 'bg-brand-orange text-white shadow-md'
              : 'text-slate-400 hover:text-slate-200'
          }`}
        >
          <RefreshCw size={18} />
          <span>{t('round_robin', lang)}</span>
        </button>
      </div>

      {/* 3. Tournament Name */}
      <div>
        <label className="block text-xs font-semibold text-slate-400 mb-1.5">
          {t('tournament_name', lang)}
        </label>
        <input
          type="text"
          value={tournamentName}
          onChange={(e) => setTournamentName(e.target.value)}
          placeholder={t('tournament_name_placeholder', lang)}
          className="w-full bg-slate-800/90 border border-slate-700 rounded-2xl px-4 py-3 text-sm text-slate-100 placeholder:text-slate-500 focus:outline-none focus:border-brand-orange transition"
        />
      </div>

      {/* 4. Match Rules */}
      <div className="bg-slate-800/50 rounded-2xl p-4 border border-slate-700/50 space-y-3">
        <h4 className="text-xs font-bold text-slate-400 uppercase tracking-wider">
          {t('match_rules', lang)}
        </h4>

        {/* Best of selector */}
        <div className="flex gap-2">
          {[1, 3, 5].map((num) => (
            <button
              key={num}
              onClick={() => setBestOf(num)}
              className={`flex-1 py-2.5 rounded-xl text-xs font-bold border transition ${
                bestOf === num
                  ? 'bg-brand-orange border-brand-orange text-white'
                  : 'bg-slate-800/80 border-slate-700 text-slate-300 hover:border-slate-600'
              }`}
            >
              {t('best_of', lang, num)}
            </button>
          ))}
        </div>

        {/* Points to win */}
        <div className="flex items-center justify-between pt-1">
          <span className="text-sm font-medium text-slate-300">
            {t('points_to_win', lang, pointsToWin)}
          </span>
          <div className="flex items-center gap-2">
            <button
              onClick={() => setPointsToWin(Math.max(5, pointsToWin - 1))}
              className="w-8 h-8 rounded-lg bg-slate-700 text-slate-200 font-bold hover:bg-slate-600 transition"
            >
              -
            </button>
            <span className="font-black text-slate-100 w-6 text-center">{pointsToWin}</span>
            <button
              onClick={() => setPointsToWin(Math.min(30, pointsToWin + 1))}
              className="w-8 h-8 rounded-lg bg-slate-700 text-slate-200 font-bold hover:bg-slate-600 transition"
            >
              +
            </button>
          </div>
        </div>

        <p className="text-xs text-slate-500">
          • {t('margin_required', lang)}
        </p>
      </div>

      {/* Doubles Sub-Mode Selector */}
      {tournamentType === 'doubles' && (
        <div className="space-y-2">
          <label className="block text-xs font-semibold text-slate-400">
            {t('doubles_mode', lang)}
          </label>
          <div className="grid grid-cols-2 gap-2">
            <button
              onClick={() => setDoublesMode('random_draw')}
              className={`py-2.5 px-3 rounded-xl text-xs font-bold border transition flex items-center justify-center gap-2 ${
                doublesMode === 'random_draw'
                  ? 'bg-brand-turquoise/20 border-brand-turquoise text-brand-turquoise'
                  : 'bg-slate-800/60 border-slate-700 text-slate-400'
              }`}
            >
              <Shuffle size={14} />
              <span>{t('doubles_random', lang)}</span>
            </button>
            <button
              onClick={() => setDoublesMode('manual_pairs')}
              className={`py-2.5 px-3 rounded-xl text-xs font-bold border transition flex items-center justify-center gap-2 ${
                doublesMode === 'manual_pairs'
                  ? 'bg-brand-turquoise/20 border-brand-turquoise text-brand-turquoise'
                  : 'bg-slate-800/60 border-slate-700 text-slate-400'
              }`}
            >
              <Users size={14} />
              <span>{t('doubles_manual', lang)}</span>
            </button>
          </div>
        </div>
      )}

      {/* Players Input Section */}
      {tournamentType === 'singles' || doublesMode === 'random_draw' ? (
        <div className="space-y-3">
          <div className="flex items-center justify-between">
            <span className="text-sm font-bold text-brand-orange">
              {tournamentType === 'singles'
                ? t('players_count', lang, activePlayersList.length)
                : `${t('players_count', lang, activePlayersList.length)} (${t('teams_count', lang, Math.floor(activePlayersList.length / 2))})`}
            </span>

            <div className="flex items-center gap-2">
              <button
                onClick={() => setShowPasteModal(true)}
                className="px-2.5 py-1.5 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-300 text-xs font-semibold flex items-center gap-1.5 transition"
              >
                <FileText size={14} />
                <span>{t('paste_list_btn', lang)}</span>
              </button>

              {activePlayersList.length > 0 && (
                <button
                  onClick={handleShuffle}
                  className="p-1.5 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-300 transition"
                  title={t('shuffle_btn', lang)}
                >
                  <Shuffle size={16} />
                </button>
              )}
            </div>
          </div>

          {/* Input field + Add button */}
          <div className="flex gap-2">
            <input
              type="text"
              value={currentInput}
              onChange={(e) => setCurrentInput(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && handleAddPlayer()}
              placeholder={t('add_player_placeholder', lang)}
              className="flex-1 bg-slate-800/90 border border-slate-700 rounded-2xl px-4 py-3 text-sm text-slate-100 placeholder:text-slate-500 focus:outline-none focus:border-brand-orange transition"
            />
            <button
              onClick={handleAddPlayer}
              className="px-5 py-3 rounded-2xl bg-brand-orange hover:bg-brand-orangeDark text-white font-bold text-sm flex items-center gap-1.5 shadow-md transition active:scale-95"
            >
              <Plus size={18} />
              <span>{t('add_btn', lang)}</span>
            </button>
          </div>

          {/* Sample Players */}
          {activePlayersList.length === 0 && (
            <button
              onClick={handleAddSamplePlayers}
              className="w-full py-2.5 rounded-xl border border-dashed border-slate-700 hover:border-brand-turquoise/50 text-brand-turquoise text-xs font-bold flex items-center justify-center gap-2 transition"
            >
              <Sparkles size={14} />
              <span>{t('sample_players_btn', lang)}</span>
            </button>
          )}

          {/* Chips */}
          {activePlayersList.length > 0 && (
            <div className="flex flex-wrap gap-2 pt-1 max-h-52 overflow-y-auto">
              {activePlayersList.map((name, idx) => (
                <div
                  key={idx}
                  className="bg-slate-800/90 border border-slate-700/80 rounded-xl px-3 py-1.5 flex items-center gap-2 text-xs text-slate-200"
                >
                  <span>
                    {idx + 1}. {name}
                  </span>
                  <button
                    onClick={() => handleRemovePlayer(idx)}
                    className="text-slate-500 hover:text-rose-400 transition"
                  >
                    <X size={14} />
                  </button>
                </div>
              ))}
            </div>
          )}

          {/* Odd Doubles Warning */}
          {isOddDoubles && (
            <div className="p-3.5 rounded-2xl bg-amber-500/10 border border-amber-500/30 text-amber-300 text-xs space-y-2">
              <div className="flex items-center gap-2 font-bold">
                <AlertTriangle size={16} className="text-amber-400 flex-shrink-0" />
                <span>{t('odd_players_warning', lang, doublesIndividual.length)}</span>
              </div>
              <div className="flex gap-2 pt-1">
                <button
                  onClick={() => setDoublesIndividual([...doublesIndividual, `احتياطي ${doublesIndividual.length + 1}`])}
                  className="flex-1 py-1.5 rounded-lg bg-amber-500 hover:bg-amber-600 text-slate-900 font-bold transition"
                >
                  {t('add_reserve_btn', lang)}
                </button>
                <button
                  onClick={() => setDoublesIndividual(doublesIndividual.slice(0, -1))}
                  className="flex-1 py-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-amber-300 font-bold transition"
                >
                  {t('remove_last_btn', lang)}
                </button>
              </div>
            </div>
          )}
        </div>
      ) : (
        /* Manual Pairs Input */
        <div className="space-y-3">
          <span className="text-sm font-bold text-brand-orange">
            {t('teams_count', lang, manualTeams.length)}
          </span>

          <div className="grid grid-cols-2 gap-2">
            <input
              type="text"
              value={teamP1}
              onChange={(e) => setTeamP1(e.target.value)}
              placeholder={t('team_player1', lang)}
              className="bg-slate-800/90 border border-slate-700 rounded-2xl px-4 py-2.5 text-xs text-slate-100 placeholder:text-slate-500 focus:outline-none focus:border-brand-orange transition"
            />
            <input
              type="text"
              value={teamP2}
              onChange={(e) => setTeamP2(e.target.value)}
              placeholder={t('team_player2', lang)}
              className="bg-slate-800/90 border border-slate-700 rounded-2xl px-4 py-2.5 text-xs text-slate-100 placeholder:text-slate-500 focus:outline-none focus:border-brand-orange transition"
            />
          </div>

          <button
            onClick={() => {
              if (teamP1.trim() && teamP2.trim()) {
                setManualTeams([...manualTeams, [teamP1.trim(), teamP2.trim()]]);
                setTeamP1('');
                setTeamP2('');
              }
            }}
            className="w-full py-2.5 rounded-xl bg-brand-orange hover:bg-brand-orangeDark text-white font-bold text-xs flex items-center justify-center gap-1.5 shadow-md transition"
          >
            <Plus size={16} />
            <span>{t('add_team_btn', lang)}</span>
          </button>

          {manualTeams.length > 0 && (
            <div className="space-y-2 max-h-48 overflow-y-auto">
              {manualTeams.map(([p1, p2], idx) => (
                <div
                  key={idx}
                  className="p-2.5 rounded-xl bg-slate-800/80 border border-slate-700 flex items-center justify-between text-xs text-slate-200"
                >
                  <span>
                    {idx + 1}. {p1} & {p2}
                  </span>
                  <button
                    onClick={() => setManualTeams(manualTeams.filter((_, i) => i !== idx))}
                    className="text-slate-500 hover:text-rose-400 transition"
                  >
                    <X size={14} />
                  </button>
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {/* Start Tournament Button */}
      <div className="pt-2">
        <button
          onClick={handleStart}
          disabled={!canStart}
          className={`w-full py-4 rounded-2xl font-black text-base flex items-center justify-center gap-2 shadow-xl transition active:scale-[0.99] ${
            canStart
              ? 'bg-brand-orange hover:bg-brand-orangeDark text-white shadow-brand-orange/30 cursor-pointer'
              : 'bg-slate-800 text-slate-600 cursor-not-allowed border border-slate-800'
          }`}
        >
          <Play size={20} className={canStart ? 'animate-pulse' : ''} />
          <span>{t('start_tournament_btn', lang)}</span>
        </button>

        {!canStart && (
          <p className="text-center text-xs text-slate-500 mt-2 font-medium">
            {t('min_players_alert', lang)}
          </p>
        )}
      </div>

      {/* Paste Names Modal */}
      {showPasteModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/75 backdrop-blur-sm animate-in fade-in duration-200">
          <div className="bg-slate-900 border border-slate-700/80 w-full max-w-md rounded-3xl p-6 shadow-2xl space-y-4">
            <div className="flex items-center justify-between">
              <h3 className="text-base font-bold text-slate-100">{t('paste_modal_title', lang)}</h3>
              <button
                onClick={() => setShowPasteModal(false)}
                className="p-1.5 rounded-xl bg-slate-800 text-slate-400 hover:text-white"
              >
                <X size={18} />
              </button>
            </div>
            <p className="text-xs text-slate-400">{t('paste_modal_hint', lang)}</p>
            <textarea
              rows={6}
              value={pasteContent}
              onChange={(e) => setPasteContent(e.target.value)}
              placeholder="محمد صلاح&#10;عمر خالد&#10;أحمد علي"
              className="w-full bg-slate-800 border border-slate-700 rounded-2xl p-3 text-xs text-slate-100 placeholder:text-slate-600 focus:outline-none focus:border-brand-orange"
            />
            <div className="flex gap-2">
              <button
                onClick={() => setShowPasteModal(false)}
                className="flex-1 py-2.5 rounded-xl border border-slate-700 text-slate-400 text-xs font-bold"
              >
                {t('close', lang)}
              </button>
              <button
                onClick={handlePasteSubmit}
                className="flex-1 py-2.5 rounded-xl bg-brand-orange text-white text-xs font-bold"
              >
                {t('paste_modal_confirm', lang)}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
