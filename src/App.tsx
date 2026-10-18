import React, { useState, useEffect, useRef } from 'react';
import {
  Trophy,
  Play,
  RotateCcw,
  Settings,
  HelpCircle,
  Dumbbell,
  Volume2,
  VolumeX,
  Vibrate,
  ArrowLeft,
  CheckCircle2,
  XCircle,
  Flame,
  Globe,
  FileCode,
  Download,
  Smartphone,
  Eye,
  Award,
  Shield,
  Layers,
  ChevronRight
} from 'lucide-react';

// Domain Types matching Kotlin GameModels
type Difficulty = 'EASY' | 'NORMAL' | 'HARD';
type Screen = 'HOME' | 'SETUP' | 'MATCH' | 'TRAINING' | 'TUTORIAL' | 'RESULTS' | 'SETTINGS' | 'PROJECT_INSPECTOR';
type GoalSector = 'TOP_LEFT' | 'BOTTOM_LEFT' | 'CENTER_HIGH' | 'CENTER_LOW' | 'TOP_RIGHT' | 'BOTTOM_RIGHT';
type ShotOutcome = 'GOAL' | 'SAVED' | 'POST_HIT' | 'CROSSBAR_HIT' | 'WIDE' | 'OVER_THE_BAR';

interface KickAttempt {
  round: number;
  isUser: boolean;
  aimX: number;
  aimY: number;
  power: number;
  sector: GoalSector;
  diveSector: GoalSector;
  outcome: ShotOutcome;
}

interface CareerStats {
  matchesPlayed: number;
  matchesWon: number;
  goalsScored: number;
  savesMade: number;
  cleanSheets: number;
  winStreak: number;
  bestStreak: number;
}

export default function App() {
  // Navigation
  const [currentScreen, setCurrentScreen] = useState<Screen>('HOME');
  const [activeTab, setActiveTab] = useState<'GAME' | 'CODE' | 'BUILD'>('GAME');

  // Match Configuration
  const [difficulty, setDifficulty] = useState<Difficulty>('NORMAL');
  const [soundEnabled, setSoundEnabled] = useState(true);
  const [hapticsEnabled, setHapticsEnabled] = useState(true);

  // Match State
  const [currentRound, setCurrentRound] = useState(1);
  const [isSuddenDeath, setIsSuddenDeath] = useState(false);
  const [isUserShooting, setIsUserShooting] = useState(true);
  const [userScore, setUserScore] = useState(0);
  const [aiScore, setAiScore] = useState(0);
  const [history, setHistory] = useState<KickAttempt[]>([]);
  const [matchWinner, setMatchWinner] = useState<'USER' | 'AI' | null>(null);

  // Flight & Animation State
  const [phase, setPhase] = useState<'AWAITING_INPUT' | 'FLIGHT' | 'OUTCOME'>('AWAITING_INPUT');
  const [currentAttempt, setCurrentAttempt] = useState<KickAttempt | null>(null);
  const [flightProgress, setFlightProgress] = useState(0);

  // Striker Controls
  const [aimX, setAimX] = useState(0);
  const [aimY, setAimY] = useState(0.5);
  const [power, setPower] = useState(0.75);

  // Training
  const [trainingScore, setTrainingScore] = useState(0);
  const [trainingAttempts, setTrainingAttempts] = useState(0);
  const [trainingOutcome, setTrainingOutcome] = useState<ShotOutcome | null>(null);

  // Local Career Stats
  const [careerStats, setCareerStats] = useState<CareerStats>(() => {
    try {
      const saved = localStorage.getItem('penalty_arena_stats');
      return saved ? JSON.parse(saved) : {
        matchesPlayed: 0,
        matchesWon: 0,
        goalsScored: 0,
        savesMade: 0,
        cleanSheets: 0,
        winStreak: 0,
        bestStreak: 0
      };
    } catch {
      return { matchesPlayed: 0, matchesWon: 0, goalsScored: 0, savesMade: 0, cleanSheets: 0, winStreak: 0, bestStreak: 0 };
    }
  });

  useEffect(() => {
    try {
      localStorage.setItem('penalty_arena_stats', JSON.stringify(careerStats));
    } catch {
      // Ignore
    }
  }, [careerStats]);

  // Audio helper
  const playBeep = (freq: number, duration: number, type: OscillatorType = 'sine') => {
    if (!soundEnabled) return;
    try {
      const ctx = new (window.AudioContext || (window as unknown as { webkitAudioContext: typeof AudioContext }).webkitAudioContext)();
      const osc = ctx.createOscillator();
      const gain = ctx.createGain();
      osc.type = type;
      osc.frequency.setValueAtTime(freq, ctx.currentTime);
      gain.gain.setValueAtTime(0.2, ctx.currentTime);
      gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + duration);
      osc.connect(gain);
      gain.connect(ctx.destination);
      osc.start();
      osc.stop(ctx.currentTime + duration);
    } catch {
      // Web Audio unsupported
    }
  };

  // Convert X, Y to Sector
  const getSectorFromCoords = (x: number, y: number): GoalSector => {
    const isLeft = x < -0.25;
    const isRight = x > 0.25;
    const isHigh = y > 0.45;
    if (isLeft && isHigh) return 'TOP_LEFT';
    if (isLeft && !isHigh) return 'BOTTOM_LEFT';
    if (isRight && isHigh) return 'TOP_RIGHT';
    if (isRight && !isHigh) return 'BOTTOM_RIGHT';
    return isHigh ? 'CENTER_HIGH' : 'CENTER_LOW';
  };

  // Shot Outcome Resolution (Exact logic matching Kotlin engine)
  const resolveShot = (sX: number, sY: number, pow: number, dive: GoalSector, diff: Difficulty): ShotOutcome => {
    if (pow > 0.93 || sY > 0.94) return 'OVER_THE_BAR';
    if (Math.abs(sX) > 0.92) return 'WIDE';
    if (Math.abs(Math.abs(sX) - 0.84) < 0.05 && sY >= 0.05 && sY <= 0.88) return 'POST_HIT';
    if (Math.abs(sY - 0.88) < 0.045 && Math.abs(sX) < 0.84) return 'CROSSBAR_HIT';
    if (pow < 0.20) return 'SAVED';

    const ballSec = getSectorFromCoords(sX, sY);
    const isExact = (ballSec === dive);
    const isSameSide = (
      (ballSec.includes('LEFT') && dive.includes('LEFT')) ||
      (ballSec.includes('RIGHT') && dive.includes('RIGHT'))
    );

    const mult = diff === 'EASY' ? 0.55 : diff === 'NORMAL' ? 0.85 : 1.15;
    let saveChance = 0.02;
    if (isExact) {
      const optimalBonus = (pow >= 0.68 && pow <= 0.85) ? 0.18 : 0;
      saveChance = Math.max(0.20, Math.min(0.92, (0.82 - optimalBonus) * mult));
    } else if (isSameSide) {
      saveChance = Math.max(0.05, Math.min(0.45, 0.28 * mult));
    }

    return Math.random() < saveChance ? 'SAVED' : 'GOAL';
  };

  // AI Goalkeeper Decision
  const getAiGoalkeeperDive = (sX: number, sY: number, diff: Difficulty): GoalSector => {
    const ballSec = getSectorFromCoords(sX, sY);
    const threshold = diff === 'EASY' ? 0.40 : diff === 'NORMAL' ? 0.65 : 0.85;
    if (Math.random() < threshold) {
      return ballSec;
    }
    const sectors: GoalSector[] = ['TOP_LEFT', 'BOTTOM_LEFT', 'CENTER_HIGH', 'CENTER_LOW', 'TOP_RIGHT', 'BOTTOM_RIGHT'];
    const otherSectors = sectors.filter(s => s !== ballSec);
    return otherSectors[Math.floor(Math.random() * otherSectors.length)];
  };

  // AI Striker Shot
  const getAiShot = (diff: Difficulty): { x: number; y: number; power: number } => {
    if (diff === 'EASY') {
      if (Math.random() < 0.2) {
        return { x: Math.random() < 0.5 ? -0.96 : 0.96, y: Math.random() * 0.7, power: 0.65 };
      }
      return { x: (Math.random() * 1.2) - 0.6, y: Math.random() * 0.65, power: 0.65 };
    }
    if (diff === 'NORMAL') {
      const targets = [
        { x: -0.7, y: 0.2 },
        { x: 0.7, y: 0.2 },
        { x: -0.7, y: 0.75 },
        { x: 0.7, y: 0.75 }
      ];
      const t = targets[Math.floor(Math.random() * targets.length)];
      return { x: t.x + (Math.random() - 0.5) * 0.2, y: t.y + (Math.random() - 0.5) * 0.15, power: 0.76 };
    }
    // HARD
    const corners = [
      { x: -0.75, y: 0.8 },
      { x: 0.75, y: 0.8 },
      { x: -0.75, y: 0.15 },
      { x: 0.75, y: 0.15 }
    ];
    const c = corners[Math.floor(Math.random() * corners.length)];
    return { x: c.x + (Math.random() - 0.5) * 0.08, y: c.y + (Math.random() - 0.5) * 0.08, power: 0.82 };
  };

  // Start new match
  const startNewMatch = () => {
    setCurrentRound(1);
    setIsSuddenDeath(false);
    setIsUserShooting(true);
    setUserScore(0);
    setAiScore(0);
    setHistory([]);
    setMatchWinner(null);
    setPhase('AWAITING_INPUT');
    setCurrentAttempt(null);
    setCurrentScreen('MATCH');
    playBeep(880, 0.15); // Start whistle
  };

  // Trigger User Kick
  const handleUserShoot = () => {
    if (phase !== 'AWAITING_INPUT' || !isUserShooting) return;
    const aiDive = getAiGoalkeeperDive(aimX, aimY, difficulty);
    executeAttempt(aimX, aimY, power, aiDive, true);
  };

  // Trigger User Goalkeeper Dive
  const handleUserDive = (sector: GoalSector) => {
    if (phase !== 'AWAITING_INPUT' || isUserShooting) return;
    const aiShot = getAiShot(difficulty);
    executeAttempt(aiShot.x, aiShot.y, aiShot.power, sector, false);
  };

  // Execute Flight & Resolution
  const executeAttempt = (sX: number, sY: number, pow: number, dive: GoalSector, isUser: boolean) => {
    playBeep(220, 0.1, 'triangle'); // Kick sound
    setPhase('FLIGHT');
    setFlightProgress(0);

    const outcome = resolveShot(sX, sY, pow, dive, difficulty);
    const attempt: KickAttempt = {
      round: currentRound,
      isUser,
      aimX: sX,
      aimY: sY,
      power: pow,
      sector: getSectorFromCoords(sX, sY),
      diveSector: dive,
      outcome
    };

    // Animate flight in steps
    let p = 0;
    const interval = setInterval(() => {
      p += 0.08;
      setFlightProgress(Math.min(1, p));
      if (p >= 1) {
        clearInterval(interval);
        finishAttempt(attempt);
      }
    }, 35);
  };

  const finishAttempt = (attempt: KickAttempt) => {
    setCurrentAttempt(attempt);
    setPhase('OUTCOME');

    // Sounds
    if (attempt.outcome === 'GOAL') {
      playBeep(587, 0.4); // cheer tone
    } else if (attempt.outcome === 'POST_HIT' || attempt.outcome === 'CROSSBAR_HIT') {
      playBeep(987, 0.15, 'sawtooth'); // metallic clang
    } else {
      playBeep(180, 0.3, 'sine'); // miss
    }

    const nextHistory = [...history, attempt];
    setHistory(nextHistory);

    let nextUserScore = userScore;
    let nextAiScore = aiScore;
    if (attempt.outcome === 'GOAL') {
      if (attempt.isUser) nextUserScore++;
      else nextAiScore++;
    }
    setUserScore(nextUserScore);
    setAiScore(nextAiScore);

    // Evaluate progression after a short pause
    setTimeout(() => {
      evaluateRoundProgress(attempt.isUser, nextUserScore, nextAiScore, nextHistory);
    }, 1800);
  };

  const evaluateRoundProgress = (
    wasUserShooting: boolean,
    uScore: number,
    aScore: number,
    allAttempts: KickAttempt[]
  ) => {
    const userAttemptsCount = allAttempts.filter(a => a.isUser).length;
    const aiAttemptsCount = allAttempts.filter(a => !a.isUser).length;

    // Early clinch check during regular 5 rounds
    if (currentRound <= 5 && !isSuddenDeath) {
      const uRem = 5 - userAttemptsCount;
      const aRem = 5 - aiAttemptsCount;

      if (uScore > aScore + aRem) {
        concludeMatch('USER', uScore, aScore, allAttempts);
        return;
      }
      if (aScore > uScore + uRem) {
        concludeMatch('AI', uScore, aScore, allAttempts);
        return;
      }
    }

    if (wasUserShooting) {
      // User just shot, now AI shoots in current round
      setIsUserShooting(false);
      setPhase('AWAITING_INPUT');
      setCurrentAttempt(null);
    } else {
      // AI just shot, round completed
      if (!isSuddenDeath && currentRound < 5) {
        setCurrentRound(r => r + 1);
        setIsUserShooting(true);
        setPhase('AWAITING_INPUT');
        setCurrentAttempt(null);
      } else if (!isSuddenDeath && currentRound === 5) {
        if (uScore > aScore) {
          concludeMatch('USER', uScore, aScore, allAttempts);
        } else if (aScore > uScore) {
          concludeMatch('AI', uScore, aScore, allAttempts);
        } else {
          // Sudden death begins
          setIsSuddenDeath(true);
          setCurrentRound(6);
          setIsUserShooting(true);
          setPhase('AWAITING_INPUT');
          setCurrentAttempt(null);
        }
      } else if (isSuddenDeath) {
        if (uScore > aScore) {
          concludeMatch('USER', uScore, aScore, allAttempts);
        } else if (aScore > uScore) {
          concludeMatch('AI', uScore, aScore, allAttempts);
        } else {
          // Advance sudden death
          setCurrentRound(r => r + 1);
          setIsUserShooting(true);
          setPhase('AWAITING_INPUT');
          setCurrentAttempt(null);
        }
      }
    }
  };

  const concludeMatch = (winner: 'USER' | 'AI', uScore: number, aScore: number, allAttempts: KickAttempt[]) => {
    setMatchWinner(winner);
    setCurrentScreen('RESULTS');
    playBeep(winner === 'USER' ? 784 : 220, 0.6);

    const isWin = (winner === 'USER');
    const userGoals = allAttempts.filter(a => a.isUser && a.outcome === 'GOAL').length;
    const userSaves = allAttempts.filter(a => !a.isUser && a.outcome === 'SAVED').length;

    setCareerStats(prev => {
      const newPlayed = prev.matchesPlayed + 1;
      const newWon = isWin ? prev.matchesWon + 1 : prev.matchesWon;
      const newStreak = isWin ? prev.winStreak + 1 : 0;
      return {
        matchesPlayed: newPlayed,
        matchesWon: newWon,
        goalsScored: prev.goalsScored + userGoals,
        savesMade: prev.savesMade + userSaves,
        cleanSheets: aScore === 0 ? prev.cleanSheets + 1 : prev.cleanSheets,
        winStreak: newStreak,
        bestStreak: Math.max(prev.bestStreak, newStreak)
      };
    });
  };

  // Practice shot in training camp
  const handlePracticeShot = () => {
    const dummyDive: GoalSector = (['TOP_LEFT', 'BOTTOM_LEFT', 'CENTER_HIGH', 'CENTER_LOW', 'TOP_RIGHT', 'BOTTOM_RIGHT'] as GoalSector[])[
      Math.floor(Math.random() * 6)
    ];
    const outcome = resolveShot(aimX, aimY, power, dummyDive, 'NORMAL');
    setTrainingAttempts(a => a + 1);
    if (outcome === 'GOAL') setTrainingScore(s => s + 1);
    setTrainingOutcome(outcome);
    playBeep(outcome === 'GOAL' ? 523 : 261, 0.2);

    setTimeout(() => {
      setTrainingOutcome(null);
    }, 1500);
  };

  return (
    <div className="flex h-screen w-full bg-slate-950 text-slate-100 overflow-hidden font-sans">
      {/* Top / Left Navigation Bar */}
      <div className="hidden lg:flex flex-col w-72 border-r border-slate-800 bg-slate-900/90 p-4 justify-between">
        <div className="space-y-6">
          <div className="flex items-center gap-3 px-2">
            <div className="h-10 w-10 rounded-xl bg-emerald-500/20 border border-emerald-500/50 flex items-center justify-center">
              <Trophy className="h-5 w-5 text-emerald-400" />
            </div>
            <div>
              <h1 className="font-black text-lg text-white tracking-wide">PENALTY ARENA</h1>
              <p className="text-xs text-emerald-400 font-medium">Native Android Football</p>
            </div>
          </div>

          <div className="space-y-1">
            <button
              onClick={() => setActiveTab('GAME')}
              className={`w-full flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-semibold transition ${
                activeTab === 'GAME'
                  ? 'bg-emerald-500 text-slate-950 shadow-md'
                  : 'text-slate-300 hover:bg-slate-800'
              }`}
            >
              <Smartphone className="h-4 w-4" />
              Interactive Simulator
            </button>

            <button
              onClick={() => setActiveTab('CODE')}
              className={`w-full flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-semibold transition ${
                activeTab === 'CODE'
                  ? 'bg-emerald-500 text-slate-950 shadow-md'
                  : 'text-slate-300 hover:bg-slate-800'
              }`}
            >
              <FileCode className="h-4 w-4" />
              Kotlin Source Inspector
            </button>

            <button
              onClick={() => setActiveTab('BUILD')}
              className={`w-full flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-semibold transition ${
                activeTab === 'BUILD'
                  ? 'bg-emerald-500 text-slate-950 shadow-md'
                  : 'text-slate-300 hover:bg-slate-800'
              }`}
            >
              <Download className="h-4 w-4" />
              Build &amp; APK Handoff
            </button>
          </div>

          {/* Quick Stats Pill */}
          <div className="p-3 bg-slate-800/60 rounded-xl border border-slate-700/60 space-y-2">
            <div className="flex items-center justify-between text-xs text-slate-400 font-semibold">
              <span>CAREER WIN RATE</span>
              <span className="text-emerald-400 font-bold">
                {careerStats.matchesPlayed > 0
                  ? Math.round((careerStats.matchesWon * 100) / careerStats.matchesPlayed)
                  : 0}%
              </span>
            </div>
            <div className="grid grid-cols-2 gap-2 text-center pt-1 border-t border-slate-700/40">
              <div>
                <p className="text-lg font-black text-white">{careerStats.matchesPlayed}</p>
                <p className="text-[10px] text-slate-400">Played</p>
              </div>
              <div>
                <p className="text-lg font-black text-emerald-400">{careerStats.matchesWon}</p>
                <p className="text-[10px] text-slate-400">Wins</p>
              </div>
            </div>
          </div>
        </div>

        {/* Squad & License Notice */}
        <div className="text-[11px] text-slate-500 p-3 bg-slate-950/60 rounded-lg border border-slate-800">
          <p className="font-semibold text-slate-400 mb-1">Squad Roster Notice</p>
          <p>Original fictional players: <strong>Leo Vance #9</strong> (Striker) and <strong>Marc Sterling #1</strong> (Goalkeeper).</p>
        </div>
      </div>

      {/* Main Content Area */}
      <div className="flex-1 flex flex-col h-full overflow-y-auto bg-slate-950">
        {/* Mobile Nav Header */}
        <div className="lg:hidden flex items-center justify-between p-3 border-b border-slate-800 bg-slate-900">
          <div className="flex items-center gap-2">
            <Trophy className="h-5 w-5 text-emerald-400" />
            <span className="font-black text-sm text-white">PENALTY ARENA</span>
          </div>
          <div className="flex items-center gap-1">
            <button
              onClick={() => setActiveTab('GAME')}
              className={`px-2.5 py-1 text-xs font-semibold rounded ${
                activeTab === 'GAME' ? 'bg-emerald-500 text-slate-950' : 'text-slate-400'
              }`}
            >
              Game
            </button>
            <button
              onClick={() => setActiveTab('CODE')}
              className={`px-2.5 py-1 text-xs font-semibold rounded ${
                activeTab === 'CODE' ? 'bg-emerald-500 text-slate-950' : 'text-slate-400'
              }`}
            >
              Code
            </button>
            <button
              onClick={() => setActiveTab('BUILD')}
              className={`px-2.5 py-1 text-xs font-semibold rounded ${
                activeTab === 'BUILD' ? 'bg-emerald-500 text-slate-950' : 'text-slate-400'
              }`}
            >
              Build
            </button>
          </div>
        </div>

        {activeTab === 'GAME' && (
          <div className="flex-1 flex items-center justify-center p-2 sm:p-4 md:p-6 bg-radial from-slate-900 to-slate-950">
            {/* Portrait Phone Frame Simulation */}
            <div className="w-full max-w-md h-[780px] max-h-[94vh] bg-slate-900 rounded-3xl border-4 border-slate-800 shadow-2xl flex flex-col overflow-hidden relative">
              {/* Phone Status Bar */}
              <div className="h-6 bg-slate-950/80 px-4 flex items-center justify-between text-[11px] text-slate-400 select-none">
                <span>09:41</span>
                <span className="font-semibold text-emerald-400">PENALTY ARENA NATIVE</span>
                <span>100%</span>
              </div>

              {/* Screen Router */}
              {currentScreen === 'HOME' && (
                <div className="flex-1 p-5 flex flex-col justify-between overflow-y-auto">
                  <div className="space-y-4">
                    <div className="flex items-center justify-between">
                      <div className="flex items-center gap-2">
                        <div className="h-8 w-8 rounded-lg bg-emerald-500/20 border border-emerald-500/40 flex items-center justify-center">
                          <Trophy className="h-4 w-4 text-emerald-400" />
                        </div>
                        <span className="font-black text-sm tracking-wider">PENALTY ARENA</span>
                      </div>
                      <button
                        onClick={() => setCurrentScreen('SETTINGS')}
                        className="p-1.5 text-slate-400 hover:text-white rounded-lg hover:bg-slate-800"
                      >
                        <Settings className="h-4 w-4" />
                      </button>
                    </div>

                    {/* Hero Banner */}
                    <div className="p-4 rounded-2xl bg-gradient-to-br from-emerald-950/60 via-slate-900 to-slate-900 border border-emerald-500/30">
                      <span className="inline-block px-2 py-0.5 rounded-full bg-emerald-500/20 text-emerald-300 font-bold text-[10px] uppercase mb-2">
                        Phase 1 • Offline Match
                      </span>
                      <h2 className="text-xl font-black text-white leading-tight">5-Round Penalty Shootout</h2>
                      <p className="text-xs text-slate-400 mt-1 mb-4">
                        Take penalties as striker and dive as goalkeeper against realistic AI difficulties with sudden death.
                      </p>
                      <button
                        onClick={() => setCurrentScreen('SETUP')}
                        className="w-full h-11 bg-emerald-500 hover:bg-emerald-400 active:scale-95 text-slate-950 font-black text-sm rounded-xl flex items-center justify-center gap-2 shadow-lg shadow-emerald-500/20 transition"
                      >
                        <Play className="h-4 w-4 fill-current" />
                        PLAY MATCH
                      </button>
                    </div>

                    {/* Secondary Navigation */}
                    <div className="grid grid-cols-2 gap-3">
                      <button
                        onClick={() => setCurrentScreen('TRAINING')}
                        className="p-3 bg-slate-800/60 hover:bg-slate-800 rounded-xl border border-slate-700/50 flex flex-col items-center gap-1.5 text-center transition"
                      >
                        <Dumbbell className="h-5 w-5 text-amber-400" />
                        <span className="font-bold text-xs text-white">Training Camp</span>
                        <span className="text-[10px] text-slate-400">Free Practice</span>
                      </button>

                      <button
                        onClick={() => setCurrentScreen('TUTORIAL')}
                        className="p-3 bg-slate-800/60 hover:bg-slate-800 rounded-xl border border-slate-700/50 flex flex-col items-center gap-1.5 text-center transition"
                      >
                        <HelpCircle className="h-5 w-5 text-emerald-400" />
                        <span className="font-bold text-xs text-white">How to Play</span>
                        <span className="text-[10px] text-slate-400">Controls &amp; Rules</span>
                      </button>
                    </div>

                    {/* Online Multiplayer Coming Soon */}
                    <div className="p-3 bg-slate-800/40 rounded-xl border border-slate-800 flex items-center justify-between">
                      <div className="flex items-center gap-2.5">
                        <Globe className="h-4 w-4 text-slate-500" />
                        <div>
                          <p className="text-xs font-bold text-slate-300">Online Multiplayer</p>
                          <p className="text-[10px] text-slate-500">PVP Rooms &amp; Matchmaking</p>
                        </div>
                      </div>
                      <span className="px-2 py-0.5 rounded bg-slate-800 text-[10px] font-bold text-slate-400 border border-slate-700">
                        PHASE 2
                      </span>
                    </div>

                    {/* Career Summary */}
                    <div className="p-3.5 bg-slate-900 rounded-xl border border-slate-800">
                      <div className="flex items-center justify-between text-xs font-bold mb-2">
                        <span className="text-slate-300">CAREER STATS</span>
                        <span className="text-emerald-400">
                          Win Rate: {careerStats.matchesPlayed > 0 ? Math.round((careerStats.matchesWon * 100) / careerStats.matchesPlayed) : 0}%
                        </span>
                      </div>
                      <div className="grid grid-cols-4 gap-2 text-center text-xs">
                        <div className="p-1.5 bg-slate-950/60 rounded">
                          <p className="font-bold text-white">{careerStats.matchesPlayed}</p>
                          <p className="text-[9px] text-slate-500">Matches</p>
                        </div>
                        <div className="p-1.5 bg-slate-950/60 rounded">
                          <p className="font-bold text-emerald-400">{careerStats.matchesWon}</p>
                          <p className="text-[9px] text-slate-500">Wins</p>
                        </div>
                        <div className="p-1.5 bg-slate-950/60 rounded">
                          <p className="font-bold text-amber-400">{careerStats.goalsScored}</p>
                          <p className="text-[9px] text-slate-500">Goals</p>
                        </div>
                        <div className="p-1.5 bg-slate-950/60 rounded">
                          <p className="font-bold text-cyan-400">{careerStats.savesMade}</p>
                          <p className="text-[9px] text-slate-500">Saves</p>
                        </div>
                      </div>
                    </div>
                  </div>

                  <p className="text-[10px] text-slate-600 text-center mt-3">
                    Leo Vance #9 (Fictional Striker) • Marc Sterling #1 (Fictional Goalkeeper)
                  </p>
                </div>
              )}

              {/* Match Setup Screen */}
              {currentScreen === 'SETUP' && (
                <div className="flex-1 p-5 flex flex-col justify-between overflow-y-auto">
                  <div className="space-y-4">
                    <div className="flex items-center gap-2">
                      <button
                        onClick={() => setCurrentScreen('HOME')}
                        className="p-1.5 text-slate-400 hover:text-white rounded-lg"
                      >
                        <ArrowLeft className="h-4 w-4" />
                      </button>
                      <h2 className="font-black text-sm text-white">MATCH SETUP</h2>
                    </div>

                    <p className="text-xs font-bold text-emerald-400 uppercase tracking-wide">
                      Select Opponent AI Difficulty
                    </p>

                    <div className="space-y-2.5">
                      {(['EASY', 'NORMAL', 'HARD'] as Difficulty[]).map(diff => {
                        const isSel = difficulty === diff;
                        return (
                          <div
                            key={diff}
                            onClick={() => setDifficulty(diff)}
                            className={`p-3 rounded-xl border cursor-pointer transition ${
                              isSel
                                ? 'bg-emerald-950/40 border-emerald-500 text-white shadow-sm'
                                : 'bg-slate-800/40 border-slate-700/60 text-slate-300 hover:bg-slate-800'
                            }`}
                          >
                            <div className="flex items-center justify-between">
                              <span className="font-bold text-xs">
                                {diff === 'EASY' ? 'Amateur (Easy)' : diff === 'NORMAL' ? 'Semi-Pro (Normal)' : 'World Class (Hard)'}
                              </span>
                              <div className={`h-3.5 w-3.5 rounded-full border flex items-center justify-center ${
                                isSel ? 'border-emerald-400 bg-emerald-500' : 'border-slate-600'
                              }`} />
                            </div>
                            <p className="text-[11px] text-slate-400 mt-1">
                              {diff === 'EASY' && 'Goalkeeper has slower reflexes and central bias. Striker strikes with moderate pace.'}
                              {diff === 'NORMAL' && 'Goalkeeper anticipates corners well. Striker targets corners with solid pace.'}
                              {diff === 'HARD' && 'Elite reflex saves and explosive strikes into upper side netting.'}
                            </p>
                          </div>
                        );
                      })}
                    </div>

                    {/* Squad Card */}
                    <div className="p-3 bg-slate-800/50 rounded-xl border border-slate-700/60 space-y-2">
                      <p className="text-xs font-bold text-amber-400">ACTIVE SQUAD (ORIGINAL FICTIONAL ROSTER)</p>
                      <div className="text-xs text-slate-300 space-y-1">
                        <p><strong>Striker:</strong> Leo Vance #9 (Fictional)</p>
                        <p><strong>Goalkeeper:</strong> Marc Sterling #1 (Fictional)</p>
                      </div>
                    </div>
                  </div>

                  <button
                    onClick={startNewMatch}
                    className="w-full h-11 bg-emerald-500 hover:bg-emerald-400 text-slate-950 font-black text-sm rounded-xl flex items-center justify-center gap-2 shadow-lg shadow-emerald-500/20 transition mt-4"
                  >
                    <Play className="h-4 w-4 fill-current" />
                    KICK OFF SHOOTOUT
                  </button>
                </div>
              )}

              {/* Match Screen */}
              {currentScreen === 'MATCH' && (
                <div className="flex-1 flex flex-col p-3 justify-between bg-slate-950">
                  {/* Top HUD */}
                  <div className="space-y-2">
                    <div className="flex items-center justify-between">
                      <button
                        onClick={() => setCurrentScreen('HOME')}
                        className="text-xs text-slate-400 hover:text-white"
                      >
                        Quit
                      </button>
                      <span className={`px-2.5 py-0.5 rounded-full text-[10px] font-black border ${
                        isSuddenDeath
                          ? 'bg-rose-500/20 border-rose-500 text-rose-400'
                          : 'bg-emerald-500/20 border-emerald-500 text-emerald-400'
                      }`}>
                        {isSuddenDeath ? `SUDDEN DEATH • R${currentRound}` : `ROUND ${currentRound} OF 5`}
                      </span>
                      <span className="text-[11px] font-bold text-amber-400">{difficulty}</span>
                    </div>

                    {/* Scoreboard Card */}
                    <div className="p-2.5 bg-slate-900 rounded-xl border border-slate-800 flex items-center justify-between">
                      <div>
                        <p className="font-black text-xs text-emerald-400">PLAYER FC</p>
                        <div className="flex gap-1 mt-1">
                          {Array.from({ length: Math.max(5, currentRound) }).map((_, i) => {
                            const att = history.filter(a => a.isUser)[i];
                            return (
                              <div
                                key={i}
                                className={`h-3 w-3 rounded-full flex items-center justify-center text-[8px] ${
                                  !att
                                    ? 'bg-slate-800 border border-slate-700'
                                    : att.outcome === 'GOAL'
                                    ? 'bg-emerald-500 text-slate-950'
                                    : 'bg-rose-500 text-white'
                                }`}
                              >
                                {att && (att.outcome === 'GOAL' ? '✓' : '✕')}
                              </div>
                            );
                          })}
                        </div>
                      </div>

                      <div className="px-3 py-1 bg-slate-950 rounded-lg text-lg font-black text-white">
                        {userScore} - {aiScore}
                      </div>

                      <div className="text-right">
                        <p className="font-black text-xs text-amber-400">RIVAL UTD</p>
                        <div className="flex gap-1 mt-1 justify-end">
                          {Array.from({ length: Math.max(5, currentRound) }).map((_, i) => {
                            const att = history.filter(a => !a.isUser)[i];
                            return (
                              <div
                                key={i}
                                className={`h-3 w-3 rounded-full flex items-center justify-center text-[8px] ${
                                  !att
                                    ? 'bg-slate-800 border border-slate-700'
                                    : att.outcome === 'GOAL'
                                    ? 'bg-emerald-500 text-slate-950'
                                    : 'bg-rose-500 text-white'
                                }`}
                              >
                                {att && (att.outcome === 'GOAL' ? '✓' : '✕')}
                              </div>
                            );
                          })}
                        </div>
                      </div>
                    </div>

                    {/* Active Role Indicator */}
                    <div className={`py-1 text-center text-xs font-bold rounded-lg border ${
                      isUserShooting
                        ? 'bg-emerald-500/15 border-emerald-500/30 text-emerald-400'
                        : 'bg-amber-500/15 border-amber-500/30 text-amber-400'
                    }`}>
                      {isUserShooting ? 'YOU ARE SHOOTING (Striker)' : 'OPPONENT SHOOTING (Defend in Goal)'}
                    </div>
                  </div>

                  {/* 2.5D Stadium Canvas Simulation */}
                  <div
                    className="flex-1 my-2 rounded-2xl relative overflow-hidden select-none bg-gradient-to-b from-slate-950 via-slate-900 to-emerald-950/80 border border-slate-800 flex flex-col justify-between p-3"
                    onClick={(e) => {
                      if (isUserShooting && phase === 'AWAITING_INPUT') {
                        const rect = e.currentTarget.getBoundingClientRect();
                        const clickX = ((e.clientX - rect.left) / rect.width) * 2 - 1;
                        const clickY = 1 - ((e.clientY - rect.top) / rect.height);
                        setAimX(Math.max(-1, Math.min(1, clickX)));
                        setAimY(Math.max(0, Math.min(1, clickY)));
                      }
                    }}
                  >
                    {/* Floodlight Cones */}
                    <div className="absolute top-0 left-0 w-44 h-44 bg-cyan-400/10 blur-3xl pointer-events-none" />
                    <div className="absolute top-0 right-0 w-44 h-44 bg-cyan-400/10 blur-3xl pointer-events-none" />

                    {/* 3D Goal Mesh */}
                    <div className="relative mx-auto mt-6 w-64 h-36 border-4 border-slate-200 border-b-0 rounded-t-sm shadow-xl flex items-center justify-center bg-slate-900/30">
                      {/* Net Pattern Grid */}
                      <div className="absolute inset-0 opacity-20 bg-[radial-gradient(#ffffff_1px,transparent_1px)] [background-size:10px_10px]" />

                      {/* Goalkeeper (Marc Sterling) */}
                      <div
                        className="absolute bottom-0 flex flex-col items-center transition-all duration-300"
                        style={{
                          left: `${50 + (phase === 'FLIGHT' || phase === 'OUTCOME'
                            ? (currentAttempt?.diveSector.includes('LEFT') ? -35 : currentAttempt?.diveSector.includes('RIGHT') ? 35 : 0) * flightProgress
                            : 0)}%`,
                          transform: 'translateX(-50%)'
                        }}
                      >
                        {/* Head */}
                        <div className="h-4 w-4 rounded-full bg-amber-200 border border-slate-700 shadow" />
                        {/* Jersey */}
                        <div className="h-6 w-8 bg-amber-500 rounded-t flex items-center justify-center text-[8px] font-black text-slate-950 shadow">
                          1
                        </div>
                        {/* Shorts */}
                        <div className="h-3 w-7 bg-slate-900 rounded-b" />
                      </div>

                      {/* Aim Crosshair Reticle (when user shooting) */}
                      {isUserShooting && phase === 'AWAITING_INPUT' && (
                        <div
                          className="absolute pointer-events-none transition-all duration-75"
                          style={{
                            left: `${((aimX + 1) / 2) * 100}%`,
                            top: `${(1 - aimY) * 100}%`,
                            transform: 'translate(-50%, -50%)'
                          }}
                        >
                          <div className="h-6 w-6 rounded-full border-2 border-emerald-400 animate-ping opacity-75" />
                          <div className="absolute inset-0 h-6 w-6 rounded-full border-2 border-emerald-400 flex items-center justify-center text-[10px] font-black text-emerald-400">
                            +
                          </div>
                        </div>
                      )}
                    </div>

                    {/* Football in Flight or at Spot */}
                    <div className="relative w-full h-24 flex items-center justify-center">
                      <div
                        className="absolute transition-all duration-75 flex items-center justify-center"
                        style={{
                          bottom: phase === 'FLIGHT' ? `${flightProgress * 120}px` : '10px',
                          left: phase === 'FLIGHT' ? `${50 + (aimX * 28 * flightProgress)}%` : '50%',
                          transform: `translate(-50%, 0) scale(${1 - flightProgress * 0.4})`
                        }}
                      >
                        {/* Ball Shadow */}
                        <div className="absolute -bottom-1 h-2 w-5 bg-black/60 rounded-full blur-[1px]" />
                        {/* Ball */}
                        <div className="h-6 w-6 rounded-full bg-white border border-slate-900 shadow flex items-center justify-center text-[8px]">
                          ⚽
                        </div>
                      </div>
                    </div>

                    {/* Outcome Overlay Banner */}
                    {phase === 'OUTCOME' && currentAttempt && (
                      <div className="absolute inset-0 bg-slate-950/70 backdrop-blur-xs flex items-center justify-center p-4">
                        <div className={`p-4 rounded-2xl border text-center ${
                          currentAttempt.outcome === 'GOAL'
                            ? 'bg-emerald-950/90 border-emerald-500 text-white'
                            : 'bg-rose-950/90 border-rose-500 text-white'
                        }`}>
                          <p className="text-xl font-black">{currentAttempt.outcome}</p>
                          <p className="text-xs text-slate-300 mt-1">
                            {currentAttempt.outcome === 'GOAL' && 'Spectacular finish into the back of the net!'}
                            {currentAttempt.outcome === 'SAVED' && 'Sensational save by the goalkeeper!'}
                            {currentAttempt.outcome === 'POST_HIT' && 'Denied by the post! Woodwork rattles!'}
                            {currentAttempt.outcome === 'CROSSBAR_HIT' && 'Hits the crossbar! Inches away!'}
                            {currentAttempt.outcome === 'WIDE' && 'Dragged wide of the target!'}
                            {currentAttempt.outcome === 'OVER_THE_BAR' && 'Skied over the bar into the stands!'}
                          </p>
                        </div>
                      </div>
                    )}
                  </div>

                  {/* Interactive Controls Bar */}
                  {phase === 'AWAITING_INPUT' && (
                    <div className="space-y-2">
                      {isUserShooting ? (
                        <div className="p-3 bg-slate-900 rounded-xl border border-slate-800 space-y-2">
                          <div className="flex items-center justify-between text-xs">
                            <span className="text-slate-400">Aim: Tap goal to position</span>
                            <span className={`font-bold ${power > 0.88 ? 'text-rose-400' : power >= 0.65 ? 'text-emerald-400' : 'text-amber-400'}`}>
                              Power: {Math.round(power * 100)}%
                            </span>
                          </div>
                          <input
                            type="range"
                            min="0.3"
                            max="1.0"
                            step="0.02"
                            value={power}
                            onChange={(e) => setPower(parseFloat(e.target.value))}
                            className="w-full accent-emerald-500 cursor-pointer"
                          />
                          <button
                            onClick={handleUserShoot}
                            className="w-full h-10 bg-emerald-500 hover:bg-emerald-400 text-slate-950 font-black text-xs rounded-lg shadow transition"
                          >
                            STRIKE PENALTY
                          </button>
                        </div>
                      ) : (
                        <div className="p-3 bg-slate-900 rounded-xl border border-slate-800 space-y-2">
                          <p className="text-xs font-bold text-center text-amber-400">
                            SELECT DIVE SECTOR TO DEFEND
                          </p>
                          <div className="grid grid-cols-3 gap-1.5">
                            {(['TOP_LEFT', 'CENTER_HIGH', 'TOP_RIGHT'] as GoalSector[]).map(sec => (
                              <button
                                key={sec}
                                onClick={() => handleUserDive(sec)}
                                className="py-2 bg-slate-800 hover:bg-slate-700 text-[10px] font-bold text-white rounded border border-slate-700 active:scale-95 transition"
                              >
                                {sec.replace('_', ' ')}
                              </button>
                            ))}
                          </div>
                          <div className="grid grid-cols-3 gap-1.5">
                            {(['BOTTOM_LEFT', 'CENTER_LOW', 'BOTTOM_RIGHT'] as GoalSector[]).map(sec => (
                              <button
                                key={sec}
                                onClick={() => handleUserDive(sec)}
                                className="py-2 bg-slate-800 hover:bg-slate-700 text-[10px] font-bold text-white rounded border border-slate-700 active:scale-95 transition"
                              >
                                {sec.replace('_', ' ')}
                              </button>
                            ))}
                          </div>
                        </div>
                      )}
                    </div>
                  )}
                </div>
              )}

              {/* Training Screen */}
              {currentScreen === 'TRAINING' && (
                <div className="flex-1 p-4 flex flex-col justify-between overflow-y-auto">
                  <div className="space-y-3">
                    <div className="flex items-center justify-between">
                      <button
                        onClick={() => setCurrentScreen('HOME')}
                        className="p-1 text-slate-400 hover:text-white"
                      >
                        <ArrowLeft className="h-4 w-4" />
                      </button>
                      <h2 className="font-black text-sm text-white">TRAINING CAMP</h2>
                      <button
                        onClick={() => {
                          setTrainingAttempts(0);
                          setTrainingScore(0);
                        }}
                        className="p-1 text-slate-400 hover:text-white"
                      >
                        <RotateCcw className="h-4 w-4" />
                      </button>
                    </div>

                    <div className="p-2.5 bg-slate-900 rounded-xl border border-slate-800 flex items-center justify-between text-xs">
                      <span className="text-emerald-400 font-bold">Goals: {trainingScore} / {trainingAttempts}</span>
                      <span className="text-amber-400 font-bold">
                        Accuracy: {trainingAttempts > 0 ? Math.round((trainingScore * 100) / trainingAttempts) : 0}%
                      </span>
                    </div>

                    <div
                      className="h-64 rounded-xl bg-gradient-to-b from-slate-950 to-emerald-950/70 border border-slate-800 relative flex items-center justify-center cursor-crosshair"
                      onClick={(e) => {
                        const rect = e.currentTarget.getBoundingClientRect();
                        const clickX = ((e.clientX - rect.left) / rect.width) * 2 - 1;
                        const clickY = 1 - ((e.clientY - rect.top) / rect.height);
                        setAimX(Math.max(-1, Math.min(1, clickX)));
                        setAimY(Math.max(0, Math.min(1, clickY)));
                      }}
                    >
                      <div className="w-56 h-32 border-4 border-slate-200 border-b-0 rounded-t-sm relative bg-slate-900/40">
                        {/* Aim point */}
                        <div
                          className="absolute pointer-events-none"
                          style={{
                            left: `${((aimX + 1) / 2) * 100}%`,
                            top: `${(1 - aimY) * 100}%`,
                            transform: 'translate(-50%, -50%)'
                          }}
                        >
                          <div className="h-5 w-5 rounded-full border-2 border-emerald-400 animate-ping opacity-75" />
                          <div className="h-5 w-5 rounded-full border-2 border-emerald-400 flex items-center justify-center text-[10px] text-emerald-400">
                            +
                          </div>
                        </div>
                      </div>

                      {trainingOutcome && (
                        <div className="absolute inset-0 bg-slate-950/80 flex items-center justify-center">
                          <span className={`px-4 py-2 rounded-xl font-black text-sm border ${
                            trainingOutcome === 'GOAL' ? 'bg-emerald-950 border-emerald-500 text-white' : 'bg-rose-950 border-rose-500 text-white'
                          }`}>
                            {trainingOutcome}
                          </span>
                        </div>
                      )}
                    </div>

                    <div className="space-y-1.5 text-xs">
                      <div className="flex justify-between text-slate-400">
                        <span>Power Calibration</span>
                        <span className="font-bold text-white">{Math.round(power * 100)}%</span>
                      </div>
                      <input
                        type="range"
                        min="0.3"
                        max="1.0"
                        step="0.02"
                        value={power}
                        onChange={(e) => setPower(parseFloat(e.target.value))}
                        className="w-full accent-emerald-500"
                      />
                    </div>
                  </div>

                  <button
                    onClick={handlePracticeShot}
                    className="w-full h-11 bg-emerald-500 hover:bg-emerald-400 text-slate-950 font-black text-sm rounded-xl mt-3 shadow transition"
                  >
                    PRACTICE KICK
                  </button>
                </div>
              )}

              {/* Tutorial Screen */}
              {currentScreen === 'TUTORIAL' && (
                <div className="flex-1 p-4 flex flex-col justify-between overflow-y-auto">
                  <div className="space-y-3">
                    <div className="flex items-center gap-2">
                      <button
                        onClick={() => setCurrentScreen('HOME')}
                        className="p-1 text-slate-400 hover:text-white"
                      >
                        <ArrowLeft className="h-4 w-4" />
                      </button>
                      <h2 className="font-black text-sm text-white">HOW TO PLAY</h2>
                    </div>

                    <div className="space-y-2.5 text-xs">
                      <div className="p-3 bg-slate-800/60 rounded-xl border border-slate-700/60">
                        <p className="font-bold text-emerald-400 mb-1">1. Aiming with Touch</p>
                        <p className="text-slate-300">Touch or drag anywhere inside the goal frame. Aiming inside corner netting makes saves much harder.</p>
                      </div>

                      <div className="p-3 bg-slate-800/60 rounded-xl border border-slate-700/60">
                        <p className="font-bold text-amber-400 mb-1">2. Power Sweet Spot</p>
                        <p className="text-slate-300">Aim for 65% – 85% power. Going over 90% risks skying the ball over the crossbar.</p>
                      </div>

                      <div className="p-3 bg-slate-800/60 rounded-xl border border-slate-700/60">
                        <p className="font-bold text-cyan-400 mb-1">3. Goalkeeper Diving</p>
                        <p className="text-slate-300">Anticipate the AI's kick corner by picking one of the 6 sectors. Matching corner and height produces a clean save.</p>
                      </div>

                      <div className="p-3 bg-slate-800/60 rounded-xl border border-slate-700/60">
                        <p className="font-bold text-rose-400 mb-1">4. Early Clinch &amp; Sudden Death</p>
                        <p className="text-slate-300">If a lead cannot mathematically be overturned (e.g. 3-0 after Round 3), the match ends immediately. Ties proceed to sudden death.</p>
                      </div>
                    </div>
                  </div>

                  <button
                    onClick={() => setCurrentScreen('TRAINING')}
                    className="w-full h-10 bg-emerald-500 hover:bg-emerald-400 text-slate-950 font-black text-xs rounded-xl mt-3 shadow"
                  >
                    GO TO TRAINING CAMP
                  </button>
                </div>
              )}

              {/* Results Screen */}
              {currentScreen === 'RESULTS' && (
                <div className="flex-1 p-5 flex flex-col items-center justify-between overflow-y-auto">
                  <div className="flex flex-col items-center text-center space-y-4 pt-4">
                    <div className={`h-16 w-16 rounded-full flex items-center justify-center border-2 ${
                      matchWinner === 'USER'
                        ? 'bg-emerald-500/20 border-emerald-500 text-emerald-400'
                        : 'bg-rose-500/20 border-rose-500 text-rose-400'
                    }`}>
                      <Trophy className="h-8 w-8" />
                    </div>

                    <div>
                      <h2 className="text-2xl font-black text-white">
                        {matchWinner === 'USER' ? 'VICTORY!' : 'DEFEAT'}
                      </h2>
                      <p className="text-xs text-slate-400 mt-1">
                        {matchWinner === 'USER' ? 'You won the penalty shootout!' : 'Rival United claimed the win.'}
                      </p>
                    </div>

                    <div className="p-4 bg-slate-900 rounded-2xl border border-slate-800 w-full space-y-3">
                      <p className="text-[10px] font-bold text-amber-400">
                        {isSuddenDeath ? 'FINAL SCORE (SUDDEN DEATH)' : 'FINAL SCORE (5 ROUNDS)'}
                      </p>
                      <div className="text-3xl font-black text-white flex items-center justify-center gap-4">
                        <span className="text-emerald-400">{userScore}</span>
                        <span className="text-slate-600">-</span>
                        <span className="text-amber-400">{aiScore}</span>
                      </div>
                    </div>
                  </div>

                  <div className="w-full space-y-2 mt-4">
                    <button
                      onClick={startNewMatch}
                      className="w-full h-11 bg-emerald-500 hover:bg-emerald-400 text-slate-950 font-black text-sm rounded-xl flex items-center justify-center gap-2 shadow transition"
                    >
                      <RotateCcw className="h-4 w-4" />
                      PLAY AGAIN
                    </button>
                    <button
                      onClick={() => setCurrentScreen('HOME')}
                      className="w-full h-10 bg-slate-800 hover:bg-slate-700 text-slate-300 font-bold text-xs rounded-xl"
                    >
                      RETURN TO ARENA
                    </button>
                  </div>
                </div>
              )}

              {/* Settings Screen */}
              {currentScreen === 'SETTINGS' && (
                <div className="flex-1 p-5 flex flex-col justify-between overflow-y-auto">
                  <div className="space-y-4">
                    <div className="flex items-center gap-2">
                      <button
                        onClick={() => setCurrentScreen('HOME')}
                        className="p-1.5 text-slate-400 hover:text-white"
                      >
                        <ArrowLeft className="h-4 w-4" />
                      </button>
                      <h2 className="font-black text-sm text-white">SETTINGS</h2>
                    </div>

                    <div className="p-3.5 bg-slate-900 rounded-xl border border-slate-800 space-y-3">
                      <div className="flex items-center justify-between">
                        <div>
                          <p className="text-xs font-bold text-white">Audio Effects</p>
                          <p className="text-[10px] text-slate-400">Crowd, kicks, and woodwork clangs</p>
                        </div>
                        <button
                          onClick={() => setSoundEnabled(!soundEnabled)}
                          className={`p-2 rounded-lg ${soundEnabled ? 'bg-emerald-500/20 text-emerald-400' : 'bg-slate-800 text-slate-500'}`}
                        >
                          {soundEnabled ? <Volume2 className="h-4 w-4" /> : <VolumeX className="h-4 w-4" />}
                        </button>
                      </div>

                      <div className="flex items-center justify-between pt-2 border-t border-slate-800">
                        <div>
                          <p className="text-xs font-bold text-white">Haptic Feedback</p>
                          <p className="text-[10px] text-slate-400">Device vibration on shot strikes</p>
                        </div>
                        <button
                          onClick={() => setHapticsEnabled(!hapticsEnabled)}
                          className={`p-2 rounded-lg ${hapticsEnabled ? 'bg-emerald-500/20 text-emerald-400' : 'bg-slate-800 text-slate-500'}`}
                        >
                          <Vibrate className="h-4 w-4" />
                        </button>
                      </div>
                    </div>

                    <button
                      onClick={() => {
                        setCareerStats({
                          matchesPlayed: 0,
                          matchesWon: 0,
                          goalsScored: 0,
                          savesMade: 0,
                          cleanSheets: 0,
                          winStreak: 0,
                          bestStreak: 0
                        });
                      }}
                      className="w-full py-2.5 text-xs text-rose-400 font-bold border border-rose-500/30 bg-rose-500/10 hover:bg-rose-500/20 rounded-xl"
                    >
                      Reset Career Stats
                    </button>
                  </div>
                </div>
              )}
            </div>
          </div>
        )}

        {activeTab === 'CODE' && (
          <div className="p-6 space-y-6 max-w-5xl mx-auto w-full">
            <div>
              <h2 className="text-xl font-black text-white">Native Android Kotlin &amp; Compose Codebase</h2>
              <p className="text-sm text-slate-400">
                All production source code is organized cleanly in the <code className="text-emerald-400 font-mono">android/</code> directory.
              </p>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div className="p-4 bg-slate-900 rounded-xl border border-slate-800">
                <span className="text-xs font-mono text-emerald-400">engine/PenaltyMatchEngine.kt</span>
                <p className="text-xs text-slate-300 mt-1 mb-3">
                  Pure, testable Kotlin class with mathematical early clinch detection, sudden death, and AI decision algorithms.
                </p>
                <div className="p-3 bg-slate-950 rounded font-mono text-[11px] text-slate-300 overflow-x-auto">
                  <code>
                    {`fun executeKick(shot: ShotSpec, dive: DiveSpec): KickAttempt {\n  val outcome = calculateOutcome(shot, dive, difficulty)\n  if (outcome.isGoal) userScore++\n  checkEarlyClinch()\n  return attempt\n}`}
                  </code>
                </div>
              </div>

              <div className="p-4 bg-slate-900 rounded-xl border border-slate-800">
                <span className="text-xs font-mono text-emerald-400">ui/components/ArenaStadiumView.kt</span>
                <p className="text-xs text-slate-300 mt-1 mb-3">
                  High-performance 2.5D Jetpack Compose Canvas engine with stadium floodlights, articulated goalkeeper, and ball physics.
                </p>
                <div className="p-3 bg-slate-950 rounded font-mono text-[11px] text-slate-300 overflow-x-auto">
                  <code>
                    {`Canvas(modifier = Modifier.fillMaxSize()) {\n  drawStadiumAtmosphere(w, h)\n  drawPerspectivePitch(w, h)\n  drawGoalAndNet(left, right, top, bottom)\n  drawGoalkeeper(centerX, groundY, ...)\n}`}
                  </code>
                </div>
              </div>

              <div className="p-4 bg-slate-900 rounded-xl border border-slate-800">
                <span className="text-xs font-mono text-emerald-400">viewmodel/PenaltyArenaViewModel.kt</span>
                <p className="text-xs text-slate-300 mt-1 mb-3">
                  Lifecycle-safe Android ViewModel exposing immutable StateFlow&lt;MatchUiState&gt; and SharedFlow&lt;GameAudioEvent&gt;.
                </p>
                <div className="p-3 bg-slate-950 rounded font-mono text-[11px] text-slate-300 overflow-x-auto">
                  <code>
                    {`class PenaltyArenaViewModel : ViewModel() {\n  private val _uiState = MutableStateFlow(MatchUiState())\n  val uiState: StateFlow<MatchUiState> = _uiState\n}`}
                  </code>
                </div>
              </div>

              <div className="p-4 bg-slate-900 rounded-xl border border-slate-800">
                <span className="text-xs font-mono text-emerald-400">test/PenaltyMatchEngineTest.kt</span>
                <p className="text-xs text-slate-300 mt-1 mb-3">
                  Automated JUnit test suite covering 5-round scoring, 3-0 early clinch lock, sudden death, and crossbar hits.
                </p>
                <div className="p-3 bg-slate-950 rounded font-mono text-[11px] text-slate-300 overflow-x-auto">
                  <code>
                    {`@Test\nfun testEarlyMatchClinch_UserWins3_0() {\n  // 3-0 lead with 2 kicks remaining -> Clinch!\n  assertTrue(engine.isMatchFinished)\n}`}
                  </code>
                </div>
              </div>
            </div>
          </div>
        )}

        {activeTab === 'BUILD' && (
          <div className="p-6 space-y-6 max-w-5xl mx-auto w-full">
            <div>
              <h2 className="text-xl font-black text-white">Android Studio Build &amp; Debug APK</h2>
              <p className="text-sm text-slate-400">
                Exact build commands and APK output artifact path for Android Studio.
              </p>
            </div>

            <div className="p-5 bg-slate-900 rounded-2xl border border-slate-800 space-y-4">
              <div className="flex items-center justify-between">
                <div>
                  <h3 className="text-sm font-bold text-white">Debug APK Artifact Location</h3>
                  <p className="text-xs font-mono text-emerald-400 mt-0.5">android/app/build/outputs/apk/debug/app-debug.apk</p>
                </div>
                <span className="px-2.5 py-1 rounded bg-emerald-500/20 text-emerald-400 text-xs font-bold border border-emerald-500/40">
                  Ready to Build
                </span>
              </div>

              <div className="p-4 bg-slate-950 rounded-xl space-y-2 text-xs font-mono text-slate-300">
                <p className="text-slate-500"># 1. Clone repository &amp; enter android folder</p>
                <p className="text-emerald-400">cd android</p>
                <p className="text-slate-500 mt-2"># 2. Run automated unit test suite</p>
                <p className="text-emerald-400">./gradlew testDebugUnitTest</p>
                <p className="text-slate-500 mt-2"># 3. Assemble complete Android Debug APK</p>
                <p className="text-emerald-400">./gradlew assembleDebug</p>
              </div>

              <div className="p-4 bg-slate-800/40 rounded-xl border border-slate-700/60 space-y-2">
                <h4 className="text-xs font-bold text-white uppercase tracking-wider">Mutually Compatible Version Matrix</h4>
                <div className="grid grid-cols-2 md:grid-cols-4 gap-3 text-xs pt-1">
                  <div>
                    <span className="text-slate-400">JDK Version:</span>
                    <p className="font-bold text-white">Java 17 (LTS)</p>
                  </div>
                  <div>
                    <span className="text-slate-400">Gradle:</span>
                    <p className="font-bold text-white">8.5</p>
                  </div>
                  <div>
                    <span className="text-slate-400">Android Gradle Plugin:</span>
                    <p className="font-bold text-white">8.3.2</p>
                  </div>
                  <div>
                    <span className="text-slate-400">Kotlin &amp; Compose:</span>
                    <p className="font-bold text-white">Kotlin 1.9.23 / 1.5.11</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
