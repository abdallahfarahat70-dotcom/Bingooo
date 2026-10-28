import { Tournament } from '../types';

const STORAGE_KEY_PREFIX = 'bingbong_tour_';
const ACTIVE_ID_KEY = 'bingbong_active_id';
const IDS_INDEX_KEY = 'bingbong_tour_ids';

export function getActiveTournament(): Tournament | null {
  try {
    const activeId = localStorage.getItem(ACTIVE_ID_KEY);
    if (!activeId) return null;
    return getTournamentById(activeId);
  } catch (e) {
    console.error('Failed to get active tournament:', e);
    return null;
  }
}

export function setActiveTournamentId(id: string | null) {
  try {
    if (id) {
      localStorage.setItem(ACTIVE_ID_KEY, id);
    } else {
      localStorage.removeItem(ACTIVE_ID_KEY);
    }
  } catch (e) {
    console.error('Failed to set active tournament id:', e);
  }
}

export function getTournamentById(id: string): Tournament | null {
  try {
    const raw = localStorage.getItem(STORAGE_KEY_PREFIX + id);
    if (!raw) return null;
    return JSON.parse(raw);
  } catch (e) {
    console.error('Failed to read tournament:', e);
    return null;
  }
}

export function getAllTournaments(): Tournament[] {
  try {
    const idsJson = localStorage.getItem(IDS_INDEX_KEY) || '[]';
    const ids: string[] = JSON.parse(idsJson);
    const list: Tournament[] = [];
    for (const id of ids) {
      const t = getTournamentById(id);
      if (t) list.push(t);
    }
    return list.sort((a, b) => b.createdAt - a.createdAt);
  } catch (e) {
    console.error('Failed to get all tournaments:', e);
    return [];
  }
}

export function saveTournament(tournament: Tournament): void {
  try {
    localStorage.setItem(STORAGE_KEY_PREFIX + tournament.id, JSON.stringify(tournament));
    const idsJson = localStorage.getItem(IDS_INDEX_KEY) || '[]';
    const ids: string[] = JSON.parse(idsJson);
    if (!ids.includes(tournament.id)) {
      ids.unshift(tournament.id);
      localStorage.setItem(IDS_INDEX_KEY, JSON.stringify(ids));
    }
    setActiveTournamentId(tournament.id);
  } catch (e) {
    console.error('Failed to save tournament:', e);
  }
}

export function deleteTournament(id: string): void {
  try {
    localStorage.removeItem(STORAGE_KEY_PREFIX + id);
    const idsJson = localStorage.getItem(IDS_INDEX_KEY) || '[]';
    const ids: string[] = JSON.parse(idsJson);
    const updated = ids.filter((currId) => currId !== id);
    localStorage.setItem(IDS_INDEX_KEY, JSON.stringify(updated));

    if (localStorage.getItem(ACTIVE_ID_KEY) === id) {
      setActiveTournamentId(updated[0] || null);
    }
  } catch (e) {
    console.error('Failed to delete tournament:', e);
  }
}
