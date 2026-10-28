import React, { useState, useEffect } from 'react';
import { Tournament } from './types';
import { Language } from './utils/strings';
import {
  deleteTournament,
  getActiveTournament,
  getAllTournaments,
  saveTournament,
  setActiveTournamentId,
} from './utils/storage';
import { SetupScreen } from './components/SetupScreen';
import { TournamentView } from './components/TournamentView';
import { HistoryModal } from './components/HistoryModal';

export const App: React.FC = () => {
  const [lang, setLang] = useState<Language>('ar');
  const [isDark, setIsDark] = useState<boolean>(true);
  const [activeTournament, setActiveTournament] = useState<Tournament | null>(() => getActiveTournament());
  const [savedTournaments, setSavedTournaments] = useState<Tournament[]>(() => getAllTournaments());
  const [showHistory, setShowHistory] = useState<boolean>(false);

  // Sync RTL / LTR layout direction
  useEffect(() => {
    document.documentElement.dir = lang === 'ar' ? 'rtl' : 'ltr';
    document.documentElement.lang = lang;
  }, [lang]);

  // Sync dark class on body
  useEffect(() => {
    if (isDark) {
      document.documentElement.classList.add('dark');
    } else {
      document.documentElement.classList.remove('dark');
    }
  }, [isDark]);

  const handleStartTournament = (tournament: Tournament) => {
    saveTournament(tournament);
    setActiveTournament(tournament);
    setSavedTournaments(getAllTournaments());
  };

  const handleUpdateTournament = (tournament: Tournament) => {
    saveTournament(tournament);
    setActiveTournament(tournament);
    setSavedTournaments(getAllTournaments());
  };

  const handleDeleteTournament = (id: string) => {
    deleteTournament(id);
    const updated = getAllTournaments();
    setSavedTournaments(updated);
    if (activeTournament?.id === id) {
      setActiveTournament(null);
    }
  };

  return (
    <div className={`min-h-screen ${isDark ? 'dark bg-[#0A0E1A] text-slate-100' : 'bg-slate-50 text-slate-900'} font-cairo transition-colors duration-200`}>
      {activeTournament ? (
        <TournamentView
          tournament={activeTournament}
          lang={lang}
          onLanguageToggle={() => setLang(lang === 'ar' ? 'en' : 'ar')}
          isDark={isDark}
          onThemeToggle={() => setIsDark(!isDark)}
          onOpenHistory={() => {
            setSavedTournaments(getAllTournaments());
            setShowHistory(true);
          }}
          onNewTournament={() => {
            setActiveTournamentId(null);
            setActiveTournament(null);
          }}
          onUpdateTournament={handleUpdateTournament}
        />
      ) : (
        <SetupScreen
          lang={lang}
          onLanguageToggle={() => setLang(lang === 'ar' ? 'en' : 'ar')}
          isDark={isDark}
          onThemeToggle={() => setIsDark(!isDark)}
          onOpenHistory={() => {
            setSavedTournaments(getAllTournaments());
            setShowHistory(true);
          }}
          onStartTournament={handleStartTournament}
        />
      )}

      {/* History Modal */}
      {showHistory && (
        <HistoryModal
          tournaments={savedTournaments}
          lang={lang}
          onSelect={(t) => {
            setActiveTournamentId(t.id);
            setActiveTournament(t);
          }}
          onDelete={handleDeleteTournament}
          onClose={() => setShowHistory(false)}
        />
      )}
    </div>
  );
};

export default App;
