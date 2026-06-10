import React, { useEffect, useMemo, useState } from 'react';
import { createRoot } from 'react-dom/client';
import { LineChart as RechartsLineChart, Line, BarChart as RechartsBarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import {
  Activity,
  BarChart3,
  BookOpen,
  Brain,
  BriefcaseBusiness,
  CheckCircle2,
  Compass,
  Home,
  LineChart,
  MinusCircle,
  Plus,
  RefreshCw,
  Search,
  Settings,
  ShieldCheck,
  TrendingUp,
  UserRound,
  Users,
  WalletCards
} from 'lucide-react';
import './styles.css';

const emptyDecision = {
  ticker: '',
  decisionType: 'watch',
  title: '',
  thesis: '',
  evidence: '',
  riskFactors: '',
  confidence: 7,
  timeHorizon: '6 months',
  exitCriteria: '',
  visibility: 'private'
};

const emptyStrategy = {
  name: '',
  description: '',
  startingCapital: 10000,
  visibility: 'private'
};

const emptyProfile = {
  handle: '',
  displayName: '',
  bio: '',
  publishedMetricIds: ['dqs', 'researchDiscipline', 'riskManagement', 'strategyConsistency']
};

const emptyAccount = {
  name: '',
  accountType: 'BROKERAGE'
};

const emptyHolding = {
  accountId: '',
  symbol: '',
  shares: '',
  costBasis: '',
  purchaseDate: '',
  manualPrice: ''
};

const navItems = [
  ['dashboard', 'Dashboard', Home],
  ['decisions', 'Decisions', BookOpen],
  ['portfolios', 'Strategies', WalletCards],
  ['investment', 'Investment', BriefcaseBusiness],
  ['reviews', 'Reviews', CheckCircle2],
  ['analytics', 'Analytics', Brain],
  ['community', 'Community', Users],
  ['profile', 'Profile', UserRound],
  ['settings', 'Settings', Settings]
];

function App() {
  const [activeView, setActiveView] = useState('dashboard');
  const [ownerId, setOwnerId] = useState(localStorage.getItem('idp.ownerId') || 'alice');
  const [authToken, setAuthToken] = useState(localStorage.getItem('idp.authToken') || '');
  const [demoMode, setDemoMode] = useState(localStorage.getItem('idp.demoMode') === 'true');
  const [authForm, setAuthForm] = useState({ username: localStorage.getItem('idp.ownerId') || 'alice', password: '' });
  const [decisions, setDecisions] = useState([]);
  const [reviews, setReviews] = useState([]);
  const [dqs, setDqs] = useState(null);
  const [behavior, setBehavior] = useState(null);
  const [strategies, setStrategies] = useState([]);
  const [selectedStrategyId, setSelectedStrategyId] = useState(null);
  const [selectedStrategy, setSelectedStrategy] = useState(null);
  const [strategyActivities, setStrategyActivities] = useState([]);
  const [strategyQuotes, setStrategyQuotes] = useState(null);
  const [strategyHistory, setStrategyHistory] = useState(null);
  const [selectedHistorySymbol, setSelectedHistorySymbol] = useState('');
  const [indicator, setIndicator] = useState(null);
  const [expandedIndicatorSymbol, setExpandedIndicatorSymbol] = useState(null);
  const [symbolIndicators, setSymbolIndicators] = useState({});
  const [publicProfile, setPublicProfile] = useState(null);
  const [portfolioSummary, setPortfolioSummary] = useState(null);
  const [decisionForm, setDecisionForm] = useState(emptyDecision);
  const [strategyForm, setStrategyForm] = useState(emptyStrategy);
  const [symbolForm, setSymbolForm] = useState({ action: 'watch', symbol: '', note: '', tags: '', visibility: 'private', quantity: '' });
  const [profileForm, setProfileForm] = useState(emptyProfile);
  const [accountForm, setAccountForm] = useState(emptyAccount);
  const [holdingForm, setHoldingForm] = useState(emptyHolding);
  const [chartRange, setChartRange] = useState('1mo');
  const [notice, setNotice] = useState('');
  const [loading, setLoading] = useState(false);
  const [showStrategyForm, setShowStrategyForm] = useState(false);
  const [showSymbolForm, setShowSymbolForm] = useState(false);
  const [showDecisionCaptureModal, setShowDecisionCaptureModal] = useState(false);
  const [pendingDecisionData, setPendingDecisionData] = useState(null);
  const [decisionCaptureForm, setDecisionCaptureForm] = useState({
    thesis: '',
    evidence: '',
    risks: '',
    comments: '',
    thesisChecked: [],
    evidenceChecked: [],
    risksChecked: [],
    exitCriteria: []
  });
  const [hasSavedDraft, setHasSavedDraft] = useState(false);

  // Auto-save draft to localStorage whenever form changes
  useEffect(() => {
    const timer = setTimeout(() => {
      if (showDecisionCaptureModal && Object.values(decisionCaptureForm).some(v => v !== '' && v !== null && (!Array.isArray(v) || v.length > 0))) {
        localStorage.setItem('idp.decisionDraft', JSON.stringify(decisionCaptureForm));
        setHasSavedDraft(true);
      }
    }, 500); // Debounce 500ms
    return () => clearTimeout(timer);
  }, [decisionCaptureForm, showDecisionCaptureModal]);

  // Load draft from localStorage on modal open
  useEffect(() => {
    if (showDecisionCaptureModal) {
      const savedDraft = localStorage.getItem('idp.decisionDraft');
      if (savedDraft) {
        try {
          const draft = JSON.parse(savedDraft);
          setHasSavedDraft(true);
        } catch (e) {
          // Invalid JSON, skip
        }
      }
    } else {
      // Clear draft indicator when modal closes
      setHasSavedDraft(false);
    }
  }, [showDecisionCaptureModal]);

  const authHeaders = useMemo(() => ({
    Authorization: `Bearer ${authToken || ownerId}`,
    'Content-Type': 'application/json'
  }), [authToken, ownerId]);

  useEffect(() => {
    localStorage.setItem('idp.ownerId', ownerId);
    if (authToken || demoMode) {
      refreshWorkspace();
    }
  }, [ownerId, authToken, demoMode]);

  useEffect(() => {
    if (strategies.length > 0 && !selectedStrategyId) {
      setSelectedStrategyId(strategies[0].id);
    }
  }, [strategies, selectedStrategyId]);

  useEffect(() => {
    if ((authToken || demoMode) && selectedStrategyId) {
      loadStrategy(selectedStrategyId);
    } else {
      setSelectedStrategy(null);
      setStrategyQuotes(null);
      setStrategyHistory(null);
      setSelectedHistorySymbol('');
      setIndicator(null);
    }
  }, [selectedStrategyId, chartRange]);

  useEffect(() => {
    const historyOptions = [
      ...(strategyHistory?.performance?.length ? ['strategy'] : []),
      ...(strategyHistory?.series?.map((series) => series.symbol).filter(Boolean) || [])
    ];
    if (historyOptions.length === 0) {
      setSelectedHistorySymbol('');
      return;
    }
    if (!selectedHistorySymbol || !historyOptions.includes(selectedHistorySymbol)) {
      setSelectedHistorySymbol(historyOptions[0]);
    }
  }, [strategyHistory, selectedHistorySymbol]);

  async function api(path, options = {}) {
    const response = await fetch(path, {
      ...options,
      headers: {
        ...authHeaders,
        ...(options.headers || {})
      }
    });
    if (response.status === 404 && options.allowNotFound) return null;
    if (!response.ok) {
      const text = await response.text();
      throw new Error(text || `Request failed with ${response.status}`);
    }
    if (response.status === 204) return null;
    return response.json();
  }

  async function register(event) {
    event?.preventDefault();
    setNotice('');
    try {
      await api('/api/users/register', {
        method: 'POST',
        body: JSON.stringify(authForm)
      });
      await login(event);
    } catch (error) {
      setNotice(error.message);
    }
  }

  async function login(event) {
    event?.preventDefault();
    setNotice('');
    try {
      const response = await api('/api/users/login', {
        method: 'POST',
        body: JSON.stringify(authForm)
      });
      const username = authForm.username.trim().toLowerCase();
      setAuthToken(response.token);
      setDemoMode(false);
      setOwnerId(username);
      localStorage.setItem('idp.authToken', response.token);
      localStorage.removeItem('idp.demoMode');
      localStorage.setItem('idp.ownerId', username);
      setNotice('Signed in.');
    } catch (error) {
      setNotice(error.message);
    }
  }

  function logout() {
    setAuthToken('');
    setDemoMode(false);
    localStorage.removeItem('idp.authToken');
    localStorage.removeItem('idp.demoMode');
    clearWorkspace();
    setNotice('');
  }

  function continueAsDemo() {
    setAuthToken('');
    setDemoMode(true);
    setOwnerId('alice');
    setAuthForm((current) => ({ ...current, username: 'alice' }));
    localStorage.removeItem('idp.authToken');
    localStorage.setItem('idp.demoMode', 'true');
    localStorage.setItem('idp.ownerId', 'alice');
    setNotice('');
  }

  function clearWorkspace() {
    setDecisions([]);
    setReviews([]);
    setDqs(null);
    setBehavior(null);
    setStrategies([]);
    setSelectedStrategyId(null);
    setSelectedStrategy(null);
    setStrategyQuotes(null);
    setStrategyHistory(null);
    setIndicator(null);
    setPublicProfile(null);
    setPortfolioSummary(null);
    setProfileForm(emptyProfile);
    setAccountForm(emptyAccount);
    setHoldingForm(emptyHolding);
  }

  async function refreshWorkspace() {
    setLoading(true);
    setNotice('');
    try {
      const [decisionData, reviewData, dqsData, behaviorData, strategyData, profileData, portfolioData] = await Promise.all([
        api('/api/decisions'),
        api('/api/reviews'),
        api('/api/analytics/dqs'),
        api('/api/analytics/behavior'),
        api('/api/strategies'),
        api('/api/profile/public', { allowNotFound: true }),
        api('/api/portfolio/summary')
      ]);
      setDecisions(decisionData.decisions || []);
      setReviews(reviewData.reviews || []);
      setDqs(dqsData);
      setBehavior(behaviorData);
      setStrategies(strategyData.strategies || []);
      setPublicProfile(profileData);
      setPortfolioSummary(portfolioData);
      setProfileForm(profileData ? {
        handle: profileData.handle,
        displayName: profileData.displayName,
        bio: profileData.bio || '',
        publishedMetricIds: selectedMetricIds(profileData.reputation)
      } : {
        ...emptyProfile,
        handle: ownerId.toLowerCase().replace(/[^a-z0-9_-]/g, '-'),
        displayName: ownerId
      });
    } catch (error) {
      setNotice(error.message);
    } finally {
      setLoading(false);
    }
  }

  async function loadStrategy(strategyId) {
    setNotice('');
    try {
      const detail = await api(`/api/strategies/${strategyId}`);
      setSelectedStrategy(detail);
      setStrategyActivities(detail.transactions || []);
      const quotes = await api(`/api/strategies/${strategyId}/quotes`);
      const history = await api(`/api/strategies/${strategyId}/history?range=${encodeURIComponent(chartRange)}`);
      setStrategyQuotes(quotes);
      setStrategyHistory(history);

      // Pre-load technical analysis for all symbols
      const allSymbols = quotes.symbols || [];
      const newSymbolIndicators = { ...symbolIndicators };
      for (const symbol of allSymbols) {
        if (!newSymbolIndicators[symbol.symbol]) {
          try {
            const analysis = await api(`/api/strategies/${strategyId}/analysis/${encodeURIComponent(symbol.symbol)}?range=1y`);
            newSymbolIndicators[symbol.symbol] = analysis;
          } catch (e) {
            // Skip if analysis fetch fails for a symbol
          }
        }
      }
      setSymbolIndicators(newSymbolIndicators);

      const firstSymbol = detail.trackedSymbols?.[0]?.symbol || quotes.symbols?.[0]?.symbol;
      setIndicator(firstSymbol
        ? await api(`/api/strategies/${strategyId}/analysis/${encodeURIComponent(firstSymbol)}?range=1y`)
        : null);
    } catch (error) {
      setNotice(error.message);
    }
  }

  async function toggleSymbolIndicators(symbol) {
    setNotice('');
    if (expandedIndicatorSymbol === symbol) {
      setExpandedIndicatorSymbol(null);
      return;
    }
    try {
      if (selectedStrategyId && !symbolIndicators[symbol]) {
        const analysis = await api(`/api/strategies/${selectedStrategyId}/analysis/${encodeURIComponent(symbol)}?range=1y`);
        setSymbolIndicators({ ...symbolIndicators, [symbol]: analysis });
      }
      setExpandedIndicatorSymbol(symbol);
    } catch (error) {
      setNotice(error.message);
    }
  }

  async function createDecision(event) {
    event.preventDefault();
    setNotice('');
    try {
      await api('/api/decisions', {
        method: 'POST',
        body: JSON.stringify({
          ...decisionForm,
          ticker: decisionForm.ticker.toUpperCase(),
          confidence: Number(decisionForm.confidence),
          evidence: lines(decisionForm.evidence),
          riskFactors: lines(decisionForm.riskFactors),
          exitCriteria: lines(decisionForm.exitCriteria)
        })
      });
      setDecisionForm(emptyDecision);
      setNotice('Decision created.');
      await refreshWorkspace();
    } catch (error) {
      setNotice(error.message);
    }
  }

  async function createStrategy(event) {
    event.preventDefault();
    setNotice('');
    try {
      const created = await api('/api/strategies', {
        method: 'POST',
        body: JSON.stringify({
          ...strategyForm,
          startingCapital: Number(strategyForm.startingCapital)
        })
      });
      setStrategyForm(emptyStrategy);
      setSelectedStrategyId(created.id);
      setNotice('Strategy created.');
      await refreshWorkspace();
    } catch (error) {
      setNotice(error.message);
    }
  }

  async function updateStrategyVisibility(visibility) {
    if (!selectedStrategyId) return;
    setNotice('');
    try {
      await api(`/api/strategies/${selectedStrategyId}/visibility`, {
        method: 'PUT',
        body: JSON.stringify({ visibility })
      });
      setNotice(`Strategy is now ${visibility}.`);
      await refreshWorkspace();
      await loadStrategy(selectedStrategyId);
    } catch (error) {
      setNotice(error.message);
    }
  }

  async function addSymbol(event) {
    event.preventDefault();
    if (!selectedStrategyId) return;
    setNotice('');
    try {
      if (symbolForm.action === 'watch') {
        await api(`/api/strategies/${selectedStrategyId}/symbols`, {
          method: 'POST',
          body: JSON.stringify({
            symbol: symbolForm.symbol.toUpperCase(),
            note: symbolForm.note,
            tags: commaList(symbolForm.tags),
            visibility: symbolForm.visibility
          })
        });
        setNotice('Watch symbol added.');
      } else {
        const response = await api(`/api/strategies/${selectedStrategyId}/transactions`, {
          method: 'POST',
          body: JSON.stringify({
            ticker: symbolForm.symbol.toUpperCase(),
            side: symbolForm.action,
            quantity: Number(symbolForm.quantity),
            decisionId: null,
            executedAt: new Date().toISOString()
          })
        });

        // Show AUTO decision capture modal for strategy transactions
        // Backend already created the decision with real-time price
        setPendingDecisionData({
          symbol: symbolForm.symbol.toUpperCase(),
          action: symbolForm.action === 'buy' ? 'BUY' : 'SELL',
          shares: Number(symbolForm.quantity),
          price: response?.price || null, // System-determined price
          transactionDate: new Date().toISOString().split('T')[0],
          isAuto: true // Mark as AUTO decision (system price, locked)
        });
        setShowDecisionCaptureModal(true);
        setDecisionCaptureForm({
          thesis: '',
          evidence: '',
          risks: '',
          comments: '',
          thesisChecked: [],
          evidenceChecked: [],
          risksChecked: [],
          exitCriteria: []
        });

        setNotice(`${labelize(symbolForm.action)} transaction recorded.`);
      }
      setSymbolForm({ action: symbolForm.action, symbol: '', note: '', tags: '', visibility: 'private', quantity: '' });
      await loadStrategy(selectedStrategyId);
    } catch (error) {
      setNotice(error.message);
    }
  }

  async function removeStrategySymbol(symbol) {
    if (!selectedStrategyId) return;
    setNotice('');
    try {
      await api(`/api/strategies/${selectedStrategyId}/symbols/${encodeURIComponent(symbol)}`, {
        method: 'DELETE'
      });
      setNotice(`${symbol} removed from watch.`);
      await loadStrategy(selectedStrategyId);
    } catch (error) {
      setNotice(error.message);
    }
  }

  function startSymbolAction(action, symbol) {
    setSymbolForm({
      action,
      symbol,
      note: '',
      tags: '',
      visibility: 'private',
      quantity: ''
    });
    setShowSymbolForm(true);
  }

  async function createAccount(event) {
    event.preventDefault();
    setNotice('');
    try {
      await api('/api/portfolio/accounts', {
        method: 'POST',
        body: JSON.stringify(accountForm)
      });
      setAccountForm(emptyAccount);
      setNotice('Investment account created.');
      await refreshWorkspace();
    } catch (error) {
      setNotice(error.message);
    }
  }

  async function addHolding(event) {
    event.preventDefault();
    if (!holdingForm.accountId) return;
    setNotice('');
    try {
      await api(`/api/portfolio/accounts/${holdingForm.accountId}/holdings`, {
        method: 'POST',
        body: JSON.stringify({
          symbol: holdingForm.symbol.toUpperCase(),
          shares: Number(holdingForm.shares),
          costBasis: holdingForm.costBasis === '' ? null : Number(holdingForm.costBasis),
          purchaseDate: holdingForm.purchaseDate || null,
          manualPrice: holdingForm.manualPrice === '' ? null : Number(holdingForm.manualPrice)
        })
      });
      setNotice('Holding added.');

      // Show decision capture modal with transaction details
      setPendingDecisionData({
        symbol: holdingForm.symbol.toUpperCase(),
        action: 'BUY',
        shares: Number(holdingForm.shares),
        price: holdingForm.costBasis === '' ? null : Number(holdingForm.costBasis) / Number(holdingForm.shares),
        transactionDate: holdingForm.purchaseDate || new Date().toISOString().split('T')[0]
      });
      setShowDecisionCaptureModal(true);
      setDecisionCaptureForm({
        thesis: '',
        evidence: '',
        risks: '',
        comments: '',
        thesisChecked: [],
        evidenceChecked: [],
        risksChecked: [],
        exitCriteria: []
      });

      setHoldingForm((current) => ({ ...emptyHolding, accountId: current.accountId }));
      await refreshWorkspace();
    } catch (error) {
      setNotice(error.message);
    }
  }

  async function deleteHolding(accountId, holdingId, holding) {
    setNotice('');
    try {
      // Capture SELL decision before deleting holding
      if (holding && holding.shares > 0) {
        const sellPrice = holding.costBasis && holding.shares > 0
          ? holding.costBasis / holding.shares
          : null;

        setPendingDecisionData({
          symbol: holding.symbol,
          action: 'SELL',
          shares: holding.shares,
          price: sellPrice,
          transactionDate: new Date().toISOString().split('T')[0]
        });
        setShowDecisionCaptureModal(true);
        setDecisionCaptureForm({
          thesis: '',
          evidence: '',
          risks: '',
          comments: '',
          thesisChecked: [],
          evidenceChecked: [],
          risksChecked: [],
          exitCriteria: []
        });
      }

      await api(`/api/portfolio/accounts/${accountId}/holdings/${holdingId}`, { method: 'DELETE' });
      setNotice('Holding removed.');
      await refreshWorkspace();
    } catch (error) {
      setNotice(error.message);
    }
  }

  async function deleteAccount(accountId) {
    setNotice('');
    try {
      await api(`/api/portfolio/accounts/${accountId}`, { method: 'DELETE' });
      setNotice('Investment account deleted.');
      await refreshWorkspace();
    } catch (error) {
      setNotice(error.message);
    }
  }

  async function publishProfile(event) {
    event.preventDefault();
    setNotice('');
    try {
      await api('/api/profile/public', {
        method: 'PUT',
        body: JSON.stringify(profileForm)
      });
      setNotice('Public profile updated.');
      await refreshWorkspace();
    } catch (error) {
      setNotice(error.message);
    }
  }

  function toggleMetric(metricId) {
    setProfileForm((current) => {
      const hasMetric = current.publishedMetricIds.includes(metricId);
      return {
        ...current,
        publishedMetricIds: hasMetric
          ? current.publishedMetricIds.filter((metric) => metric !== metricId)
          : [...current.publishedMetricIds, metricId]
      };
    });
  }

  const today = new Date().toISOString().slice(0, 10);
  const pendingReviews = reviews.filter((review) => review.status === 'pending');
  const overdueReviews = pendingReviews.filter((review) => review.dueDate < today);
  const recentDecisions = [...decisions].sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt)).slice(0, 6);
  const activeStrategies = strategies.slice(0, 4);
  const activity = recentActivity(decisions, reviews, strategies, publicProfile);
  const funnel = decisionFunnel(decisions, reviews, strategies);

  if (!authToken && !demoMode) {
    return (
      <AuthLanding
        authForm={authForm}
        setAuthForm={setAuthForm}
        login={login}
        register={register}
        continueAsDemo={continueAsDemo}
        notice={notice}
      />
    );
  }

  return (
    <div className="shell">
      <aside className="sidebar">
        <div className="brand">
          <div className="brandMark"><TrendingUp size={22} /></div>
          <div>
            <strong>IDP</strong>
            <span>Investor development</span>
          </div>
        </div>
        <nav className="nav">
          {navItems.map(([id, label, Icon]) => (
            <button className={activeView === id ? 'active' : ''} type="button" key={id} onClick={() => setActiveView(id)}>
              <Icon size={18} />
              {label}
            </button>
          ))}
        </nav>
        <div className="sidebarAuth">
          <strong>{authToken ? 'Signed in' : 'Demo mode'}</strong>
          <small>{authToken ? 'Using a local bearer token.' : 'Using the sample development identity.'}</small>
          <button className="linkButton" type="button" onClick={logout}>{authToken ? 'Logout' : 'Exit demo'}</button>
        </div>
        <div className="sidebarProfile">
          <span>{publicProfile ? `@${publicProfile.handle}` : ownerId}</span>
          <strong>{authToken ? 'Signed in' : 'Demo workspace'}</strong>
          <small>{pendingReviews.length} pending reviews</small>
        </div>
      </aside>

      <div className="mainShell">
        <header className="topbar">
          <div>
            <p className="eyebrow">Investor Development Platform</p>
            <h1>{viewTitle(activeView)}</h1>
          </div>
          <div className="identity">
            <label htmlFor="ownerId">Investor</label>
            <input id="ownerId" value={ownerId} disabled={!demoMode} onChange={(event) => setOwnerId(event.target.value || 'alice')} />
            <button type="button" className="iconButton" onClick={refreshWorkspace} title="Refresh workspace">
              <RefreshCw size={18} />
            </button>
          </div>
        </header>

        {notice && <div className="notice">{notice}</div>}

        <main className="layout">
          {activeView === 'dashboard' && (
            <Dashboard
              dqs={dqs}
              behavior={behavior}
              decisions={decisions}
              reviews={reviews}
              pendingReviews={pendingReviews}
              overdueReviews={overdueReviews}
              recentDecisions={recentDecisions}
              strategies={activeStrategies}
              activity={activity}
              funnel={funnel}
              publicProfile={publicProfile}
              chartRange={chartRange}
              setChartRange={setChartRange}
              setActiveView={setActiveView}
              symbolIndicators={symbolIndicators}
              selectedStrategyId={selectedStrategyId}
              selectedStrategy={selectedStrategy}
              strategyQuotes={strategyQuotes}
            />
          )}
          {activeView === 'decisions' && (
            <DecisionsView decisions={decisions} decisionForm={decisionForm} setDecisionForm={setDecisionForm} createDecision={createDecision} />
          )}
          {activeView === 'portfolios' && (
            <PortfolioView
              strategies={strategies}
              selectedStrategyId={selectedStrategyId}
              setSelectedStrategyId={setSelectedStrategyId}
              selectedStrategy={selectedStrategy}
              strategyActivities={strategyActivities}
              strategyForm={strategyForm}
              setStrategyForm={setStrategyForm}
              createStrategy={createStrategy}
              updateStrategyVisibility={updateStrategyVisibility}
              symbolForm={symbolForm}
              setSymbolForm={setSymbolForm}
              addSymbol={addSymbol}
              removeStrategySymbol={removeStrategySymbol}
              startSymbolAction={startSymbolAction}
              strategyQuotes={strategyQuotes}
              strategyHistory={strategyHistory}
              selectedHistorySymbol={selectedHistorySymbol}
              setSelectedHistorySymbol={setSelectedHistorySymbol}
              expandedIndicatorSymbol={expandedIndicatorSymbol}
              toggleSymbolIndicators={toggleSymbolIndicators}
              symbolIndicators={symbolIndicators}
              chartRange={chartRange}
              setChartRange={setChartRange}
              showStrategyForm={showStrategyForm}
              setShowStrategyForm={setShowStrategyForm}
              showSymbolForm={showSymbolForm}
              setShowSymbolForm={setShowSymbolForm}
            />
          )}
          {activeView === 'investment' && (
            <InvestmentWorkspaceView
              portfolioSummary={portfolioSummary}
              accountForm={accountForm}
              setAccountForm={setAccountForm}
              createAccount={createAccount}
              holdingForm={holdingForm}
              setHoldingForm={setHoldingForm}
              addHolding={addHolding}
              deleteHolding={deleteHolding}
              deleteAccount={deleteAccount}
            />
          )}
          {activeView === 'reviews' && <ReviewsView reviews={reviews} pendingReviews={pendingReviews} overdueReviews={overdueReviews} />}
          {activeView === 'analytics' && <AnalyticsView dqs={dqs} behavior={behavior} decisions={decisions} reviews={reviews} />}
          {activeView === 'community' && <CommunityView publicProfile={publicProfile} strategies={strategies} ownerId={ownerId} />}
          {activeView === 'profile' && (
            <ProfileView
              publicProfile={publicProfile}
              profileForm={profileForm}
              setProfileForm={setProfileForm}
              toggleMetric={toggleMetric}
              publishProfile={publishProfile}
            />
          )}
          {activeView === 'settings' && <SettingsView ownerId={ownerId} authToken={authToken} demoMode={demoMode} />}
        </main>
      </div>
      {loading && <div className="loading">Refreshing workspace...</div>}
      <DecisionCaptureModal
        isOpen={showDecisionCaptureModal}
        onClose={() => {
          setShowDecisionCaptureModal(false);
          handleClearDraft();
        }}
        pendingDecision={pendingDecisionData}
        formData={decisionCaptureForm}
        onFormChange={setDecisionCaptureForm}
        hasSavedDraft={hasSavedDraft}
        onSubmit={async (decision, isAutoDecision) => {
          setLoading(true);
          try {
            const payload = {
              symbol: decision.symbol,
              action: decision.action,
              quantity: decision.shares,
              price: decision.price,
              transaction_date: decision.transactionDate,
              thesis: decision.thesis || null,
              evidence: decision.evidence || null,
              risks: decision.risks || null,
              comments: decision.comments || null
            };

            // Route to appropriate endpoint based on decision source
            const endpoint = isAutoDecision ? '/api/decisions/auto' : '/api/decisions/manual';
            const decisionResponse = await api(endpoint, {
              method: 'POST',
              body: JSON.stringify(payload)
            });

            // Log thesis/evidence/risks details
            if (decisionResponse?.id && (thesis || evidence || risks)) {
              await api(`/api/decisions/${decisionResponse.id}/log-details`, {
                method: 'POST',
                body: JSON.stringify({
                  thesis,
                  evidence,
                  risks,
                  comments
                })
              });
            }

            // Add exit criteria alerts if provided
            if (decisionResponse?.id && (formData.exitCriteria || []).length > 0) {
              for (const criteria of formData.exitCriteria) {
                await api(`/api/decisions/${decisionResponse.id}/exit-criteria`, {
                  method: 'POST',
                  body: JSON.stringify({
                    condition_type: criteria.type,
                    condition_value: criteria.value,
                    description: criteria.description || ''
                  })
                });
              }
            }

            setShowDecisionCaptureModal(false);
            setDecisionCaptureForm({
              thesis: '',
              evidence: '',
              risks: '',
              comments: '',
              thesisChecked: [],
              evidenceChecked: [],
              risksChecked: [],
              exitCriteria: []
            });
            setNotice('Decision captured with exit criteria.');
          } catch (error) {
            setNotice(error.message);
          } finally {
            setLoading(false);
          }
        }}
        isSubmitting={loading}
      />
    </div>
  );
}

function AuthLanding({ authForm, setAuthForm, login, register, continueAsDemo, notice }) {
  return (
    <main className="authPage">
      <section className="authHero">
        <div className="brand authBrand">
          <div className="brandMark"><TrendingUp size={22} /></div>
          <div>
            <strong>IDP</strong>
            <span>Investor development</span>
          </div>
        </div>
        <p className="eyebrow">Investor Development Platform</p>
        <h1>Sign in to your investor workspace</h1>
        <p>Track decisions, reviews, strategies, and public reputation signals from one private workspace.</p>
      </section>

      <section className="authPanel" aria-label="Account access">
        <div>
          <h2>Account Access</h2>
          <p>Use a local account for your workspace, or open the sample demo workspace.</p>
        </div>
        {notice && <div className="authNotice">{notice}</div>}
        <form className="authForm" onSubmit={login}>
          <label>
            Username
            <input value={authForm.username} onChange={(event) => setAuthForm({ ...authForm, username: event.target.value })} autoComplete="username" required />
          </label>
          <label>
            Password
            <input type="password" value={authForm.password} onChange={(event) => setAuthForm({ ...authForm, password: event.target.value })} autoComplete="current-password" required />
          </label>
          <div className="authButtons">
            <button className="primary" type="submit">Login</button>
            <button className="secondaryButton" type="button" onClick={register}>Register</button>
          </div>
        </form>
        <button className="ghostButton" type="button" onClick={continueAsDemo}>Continue as demo user</button>
      </section>
    </main>
  );
}

function Dashboard({
  dqs,
  behavior,
  decisions,
  reviews,
  pendingReviews,
  overdueReviews,
  recentDecisions,
  strategies,
  activity,
  funnel,
  publicProfile,
  chartRange,
  setChartRange,
  setActiveView,
  symbolIndicators = {},
  selectedStrategyId = null,
  selectedStrategy = null,
  strategyQuotes = null
}) {
  return (
    <>
      <section className="scoreGrid">
        <Metric icon={<Brain />} label="Decision Quality" value={dqs?.score ?? 0} accent={scoreLabel(dqs?.score)} />
        <Metric icon={<BookOpen />} label="Research Discipline" value={behavior?.researchDisciplineScore ?? 0} accent={scoreLabel(behavior?.researchDisciplineScore)} />
        <Metric icon={<ShieldCheck />} label="Risk Management" value={behavior?.riskDisciplineScore ?? 0} accent={scoreLabel(behavior?.riskDisciplineScore)} />
        <Metric icon={<Activity />} label="Behavioral Score" value={behavior?.behavioralScore ?? 0} accent={scoreLabel(behavior?.behavioralScore)} />
      </section>

      <section className="dashboardGrid">
        <Panel className="span2" title="Performance Overview" icon={<LineChart />}>
          <div className="rangeTabs">
            {[
              ['1mo', '1M'],
              ['3mo', '3M'],
              ['6mo', '6M'],
              ['1y', '1Y'],
              ['5y', '5Y']
            ].map(([value, label]) => (
              <button className={chartRange === value ? 'selected' : ''} type="button" key={value} onClick={() => setChartRange(value)}>{label}</button>
            ))}
          </div>
          <PerformanceSketch decisions={decisions} strategies={strategies} range={rangeLabel(chartRange)} />
          <div className="returnGrid">
            <Badge label="All Strategies" value={`${strategies.length} active`} />
            <Badge label="S&P 500" value="context" />
            <Badge label="NASDAQ 100" value="context" />
          </div>
        </Panel>

        <Panel title="Next Reviews" icon={<CheckCircle2 />} action="View all" onAction={() => setActiveView('reviews')}>
          <div className="list compact">
            {pendingReviews.slice(0, 5).map((review) => (
              <article className="listItem" key={review.id}>
                <strong>{review.reviewType}</strong>
                <span>Decision #{review.decisionId}</span>
                <small>{review.dueDate} · {review.dueDate < new Date().toISOString().slice(0, 10) ? 'overdue' : 'scheduled'}</small>
              </article>
            ))}
            {pendingReviews.length === 0 && <Empty text="No scheduled reviews yet." />}
          </div>
        </Panel>

        <Panel title="Recent Activity" icon={<Activity />}>
          <div className="timeline">
            {activity.map((item) => (
              <div className="timelineItem" key={item.id}>
                <span />
                <div>
                  <strong>{item.title}</strong>
                  <small>{item.detail}</small>
                </div>
              </div>
            ))}
          </div>
        </Panel>

        <Panel title="Active Strategies" icon={<WalletCards />} action="View all" onAction={() => setActiveView('portfolios')}>
          <div className="strategyCards">
            {strategies.map((strategy) => (
              <article className="miniCard" key={strategy.id}>
                <strong>{strategy.name}</strong>
                <span>{currency(strategy.startingCapital)}</span>
                <small>{strategy.visibility} · research surface ready</small>
              </article>
            ))}
            {strategies.length === 0 && <Empty text="No strategies yet." />}
          </div>
        </Panel>

        <Panel title="Technical Outlook" icon={<TrendingUp />} action={selectedStrategy ? 'View full' : undefined} onAction={() => selectedStrategy && setActiveView('portfolios')}>
          {selectedStrategy && strategyQuotes?.symbols ? (
            <div style={{ display: 'grid', gap: '10px' }}>
              {strategyQuotes.symbols.slice(0, 3).map((quote) => {
                const analysis = symbolIndicators[quote.symbol];
                const confidence = analysis?.recommendation?.confidence || 'Low';
                const regime = analysis?.recommendation?.label || 'N/A';
                const regimeColor = regime.toLowerCase().includes('bearish') ? '#dc2626'
                                   : regime.toLowerCase().includes('bullish') ? '#16a34a'
                                   : '#9facbd';
                const confidenceColor = confidence === 'High' ? '#dcfce7' : confidence === 'Medium' ? '#fef3c7' : '#fee2e2';
                const confidenceTextColor = confidence === 'High' ? '#166534' : confidence === 'Medium' ? '#92400e' : '#991b1b';

                return (
                  <article key={quote.symbol} style={{
                    padding: '8px 10px',
                    background: '#f9fbfb',
                    border: '1px solid #e2e8f0',
                    borderRadius: '5px',
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center'
                  }}>
                    <div>
                      <strong style={{ fontSize: '0.9rem', display: 'block' }}>{quote.symbol}</strong>
                      <small style={{ color: regimeColor, fontWeight: 600 }}>
                        {regime.substring(0, 12)}
                      </small>
                    </div>
                    <span style={{
                      background: confidenceColor,
                      color: confidenceTextColor,
                      padding: '2px 6px',
                      borderRadius: '3px',
                      fontSize: '0.7rem',
                      fontWeight: 600
                    }}>
                      {confidence}
                    </span>
                  </article>
                );
              })}
            </div>
          ) : (
            <Empty text="Select a strategy to view technical outlook." />
          )}
        </Panel>

        <Panel title="Behavior Insights" icon={<Brain />} action="Full report" onAction={() => setActiveView('analytics')}>
          <div className="list compact">
            {(behavior?.insights || []).slice(0, 4).map((insight) => (
              <article className="listItem" key={insight.title}>
                <strong>{insight.title}</strong>
                <span>{insight.detail}</span>
                <small>{insight.type}</small>
              </article>
            ))}
            {(!behavior?.insights || behavior.insights.length === 0) && <Empty text="No behavior insights yet." />}
          </div>
        </Panel>

        <Panel title="Decision Funnel" icon={<BarChart3 />}>
          <div className="funnel">
            {funnel.map((stage) => (
              <div className="funnelRow" key={stage.label}>
                <span>{stage.label}</span>
                <div><i style={{ width: `${stage.percent}%` }} /></div>
                <strong>{stage.value}</strong>
              </div>
            ))}
          </div>
        </Panel>

        <Panel title="Market Snapshot" icon={<Compass />}>
          <div className="marketGrid">
            {[
              ['S&P 500', 'Provider pending'],
              ['NASDAQ 100', 'Provider pending'],
              ['DOW JONES', 'Provider pending'],
              ['VIX', 'Provider pending'],
              ['10Y Treasury', 'Provider pending']
            ].map(([label, value]) => <Badge key={label} label={label} value={value} />)}
          </div>
        </Panel>

        <Panel title="Recent Decisions" icon={<BookOpen />} action="View all" onAction={() => setActiveView('decisions')}>
          <DecisionTable decisions={recentDecisions} />
        </Panel>

        <Panel title="Community Signals" icon={<Users />} action="Profile" onAction={() => setActiveView('profile')}>
          <div className="communityCard">
            <strong>{publicProfile ? `@${publicProfile.handle}` : 'Publish profile to appear here'}</strong>
            <span>{publicProfile ? `${publicProfile.publishedStrategies.length} published strategies` : 'Community feed is ready for public-profile data.'}</span>
          </div>
        </Panel>
      </section>
    </>
  );
}

function DecisionsView({ decisions, decisionForm, setDecisionForm, createDecision }) {
  const [searchTicker, setSearchTicker] = React.useState('');
  const [filterType, setFilterType] = React.useState('all');
  const [filterStatus, setFilterStatus] = React.useState('all');
  const [searchThesisRisk, setSearchThesisRisk] = React.useState('');
  const [fromDate, setFromDate] = React.useState('');
  const [toDate, setToDate] = React.useState('');
  const [showAdvancedFilters, setShowAdvancedFilters] = React.useState(false);
  const [selectedDecision, setSelectedDecision] = React.useState(null);
  const [editForm, setEditForm] = React.useState({});
  const [triggeredAlert, setTriggeredAlert] = React.useState(null);

  // Get unique tickers for autocomplete
  const uniqueTickers = React.useMemo(() => {
    return [...new Set(decisions.map(d => d.ticker || d.symbol).filter(Boolean))].sort();
  }, [decisions]);

  const filteredDecisions = decisions.filter(d => {
    const matchesTicker = searchTicker === '' || (d.ticker || d.symbol || '').toUpperCase().includes(searchTicker.toUpperCase());
    const matchesType = filterType === 'all' || d.decisionType === filterType;
    const matchesStatus = filterStatus === 'all' || d.status === filterStatus;

    // Thesis/Risk keyword search
    const searchLower = searchThesisRisk.toLowerCase();
    const matchesThesisRisk = searchThesisRisk === '' ||
      (d.thesis && d.thesis.toLowerCase().includes(searchLower)) ||
      (d.risks && d.risks.toLowerCase().includes(searchLower)) ||
      (d.evidence && d.evidence.toLowerCase().includes(searchLower));

    // Date range filtering
    const decisionDate = new Date(d.createdAt);
    const matchesFromDate = !fromDate || decisionDate >= new Date(fromDate);
    const matchesToDate = !toDate || decisionDate <= new Date(toDate + 'T23:59:59');

    return matchesTicker && matchesType && matchesStatus && matchesThesisRisk && matchesFromDate && matchesToDate;
  });

  const handleOpenDecisionDetail = React.useCallback((decision) => {
    setSelectedDecision(decision);
    setEditForm({
      thesis: decision.thesis || '',
      evidence: decision.evidence || '',
      risks: decision.risks || '',
      comments: decision.comments || '',
      exitCriteria: decision.exitCriteria || []
    });
  }, []);

  const handleCloseDecision = React.useCallback((decisionId, decision) => {
    // TODO: Call close decision API with exit price from triggered alert
    const alertedPrice = decision.alerts?.find(a => a.triggered)?.value;
    if (alertedPrice) {
      console.log(`Closing decision ${decisionId} at price $${alertedPrice}`);
      // API call would go here: closeDecision(decisionId, { exitPrice: alertedPrice })
    }
  }, []);

  const decisionStats = {
    total: decisions.length,
    active: decisions.filter(d => d.status === 'active').length,
    closed: decisions.filter(d => d.status === 'closed').length,
    watch: decisions.filter(d => d.decisionType === 'watch').length
  };

  return (
    <>
      <section className="workspaceGrid">
        <Panel title="Create Decision" icon={<Plus />}>
          <DecisionForm decisionForm={decisionForm} setDecisionForm={setDecisionForm} createDecision={createDecision} />
        </Panel>
      </section>

      <section style={{ padding: '0 clamp(12px, 2vw, 28px) 28px' }}>
        <Panel title="Decision Journal" icon={<BookOpen />}>
          <div style={{ marginBottom: '16px', display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '12px' }}>
            <div style={{ background: '#f9fbfb', padding: '10px', borderRadius: '6px', border: '1px solid #e4e7ec' }}>
              <small style={{ color: '#667085' }}>Total</small>
              <div style={{ fontSize: '1.3rem', fontWeight: 700, color: '#20242a' }}>{decisionStats.total}</div>
            </div>
            <div style={{ background: '#f0fdf4', padding: '10px', borderRadius: '6px', border: '1px solid #dcfce7' }}>
              <small style={{ color: '#667085' }}>Active</small>
              <div style={{ fontSize: '1.3rem', fontWeight: 700, color: '#16a34a' }}>{decisionStats.active}</div>
            </div>
            <div style={{ background: '#f9fbfb', padding: '10px', borderRadius: '6px', border: '1px solid #e4e7ec' }}>
              <small style={{ color: '#667085' }}>Closed</small>
              <div style={{ fontSize: '1.3rem', fontWeight: 700, color: '#526071' }}>{decisionStats.closed}</div>
            </div>
            <div style={{ background: '#f9fbfb', padding: '10px', borderRadius: '6px', border: '1px solid #e4e7ec' }}>
              <small style={{ color: '#667085' }}>Watch List</small>
              <div style={{ fontSize: '1.3rem', fontWeight: 700, color: '#0f766e' }}>{decisionStats.watch}</div>
            </div>
          </div>

          {/* Filter Row 1: Search + Quick Filters */}
          <div style={{ marginBottom: '12px', display: 'grid', gridTemplateColumns: '2fr 1fr 1fr', gap: '8px', width: '100%' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', padding: '10px 12px', background: '#ffffff', borderRadius: '6px', border: '1px solid #d7dce2' }}>
              <Search size={16} style={{ color: '#9facbd', flexShrink: 0 }} />
              <input
                type="text"
                placeholder="Search by ticker..."
                value={searchTicker}
                onChange={(e) => setSearchTicker(e.target.value)}
                list="ticker-list"
                style={{ flex: 1, border: 'none', background: 'transparent', fontSize: '0.9rem', outline: 'none', color: '#20242a', padding: 0, width: '100%' }}
              />
              <datalist id="ticker-list">
                {uniqueTickers.map(ticker => (
                  <option key={ticker} value={ticker} />
                ))}
              </datalist>
            </div>
            <select
              value={filterType}
              onChange={(e) => setFilterType(e.target.value)}
              style={{ padding: '10px 11px', background: '#ffffff', border: '1px solid #d7dce2', borderRadius: '7px', fontSize: '0.9rem', color: '#20242a' }}
            >
              <option value="all">All types</option>
              <option value="BUY">Buy</option>
              <option value="SELL">Sell</option>
              <option value="watch">Watch</option>
              <option value="buy">Buy (Manual)</option>
              <option value="sell">Sell (Manual)</option>
              <option value="avoid">Avoid</option>
            </select>
            <select
              value={filterStatus}
              onChange={(e) => setFilterStatus(e.target.value)}
              style={{ padding: '10px 11px', background: '#ffffff', border: '1px solid #d7dce2', borderRadius: '7px', fontSize: '0.9rem', color: '#20242a' }}
            >
              <option value="all">All status</option>
              <option value="open">Open</option>
              <option value="active">Active</option>
              <option value="closed">Closed</option>
              <option value="archived">Archived</option>
            </select>
          </div>

          {/* Filter Row 2: Thesis/Risk Search */}
          <div style={{ marginBottom: '12px', display: 'flex', gap: '8px', width: '100%' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', padding: '10px 12px', background: '#ffffff', borderRadius: '6px', border: '1px solid #d7dce2', flex: 1 }}>
              <Search size={16} style={{ color: '#9facbd', flexShrink: 0 }} />
              <input
                type="text"
                placeholder="Search thesis, evidence, or risks..."
                value={searchThesisRisk}
                onChange={(e) => setSearchThesisRisk(e.target.value)}
                style={{ flex: 1, border: 'none', background: 'transparent', fontSize: '0.9rem', outline: 'none', color: '#20242a', padding: 0 }}
              />
            </div>
            {searchThesisRisk && (
              <button onClick={() => setSearchThesisRisk('')} style={{ padding: '8px 12px', fontSize: '0.8rem', color: '#dc2626', background: '#fef2f2', border: '1px solid #fee2e2', borderRadius: '6px', cursor: 'pointer', fontWeight: 500 }}>
                Clear
              </button>
            )}
          </div>

          {/* Advanced Filters Toggle */}
          <div style={{ marginBottom: '12px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <button
              onClick={() => setShowAdvancedFilters(!showAdvancedFilters)}
              style={{
                border: 'none',
                background: 'transparent',
                color: '#13a79a',
                cursor: 'pointer',
                fontSize: '0.85rem',
                fontWeight: 500,
                padding: '4px 0',
                display: 'flex',
                alignItems: 'center',
                gap: '6px'
              }}
            >
              {showAdvancedFilters ? '▼' : '▶'} Date Range Filter
            </button>
            {(fromDate || toDate) && (
              <button
                onClick={() => {
                  setFromDate('');
                  setToDate('');
                }}
                style={{
                  border: 'none',
                  background: '#fef2f2',
                  color: '#dc2626',
                  cursor: 'pointer',
                  fontSize: '0.75rem',
                  fontWeight: 600,
                  padding: '4px 8px',
                  borderRadius: '4px'
                }}
              >
                Clear dates
              </button>
            )}
          </div>

          {/* Advanced Filters: Date Range */}
          {showAdvancedFilters && (
            <div style={{ marginBottom: '12px', display: 'grid', gridTemplateColumns: '1fr 1fr auto', gap: '8px', padding: '12px', background: '#f9fbfb', borderRadius: '6px', border: '1px solid #e4e7ec' }}>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                <label style={{ fontSize: '0.75rem', fontWeight: 600, color: '#667085', textTransform: 'uppercase' }}>From</label>
                <input
                  type="date"
                  value={fromDate}
                  onChange={(e) => setFromDate(e.target.value)}
                  style={{ padding: '8px 10px', background: '#ffffff', border: '1px solid #d7dce2', borderRadius: '6px', fontSize: '0.9rem', color: '#20242a' }}
                />
              </div>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                <label style={{ fontSize: '0.75rem', fontWeight: 600, color: '#667085', textTransform: 'uppercase' }}>To</label>
                <input
                  type="date"
                  value={toDate}
                  onChange={(e) => setToDate(e.target.value)}
                  style={{ padding: '8px 10px', background: '#ffffff', border: '1px solid #d7dce2', borderRadius: '6px', fontSize: '0.9rem', color: '#20242a' }}
                />
              </div>
              <div style={{ display: 'flex', alignItems: 'flex-end' }}>
                <span style={{ fontSize: '0.8rem', color: '#9facbd' }}>
                  {filteredDecisions.length} decision{filteredDecisions.length !== 1 ? 's' : ''}
                </span>
              </div>
            </div>
          )}


          <DecisionJournalTimeline decisions={filteredDecisions} onCloseDecision={handleCloseDecision} onCardClick={handleOpenDecisionDetail} showEmpty={filteredDecisions.length === 0} />
          {selectedDecision && (
            <DecisionDetailModal
              decision={selectedDecision}
              onClose={() => setSelectedDecision(null)}
              editForm={editForm}
              setEditForm={setEditForm}
              api={api}
              onSaveSuccess={() => {
                setSelectedDecision(null);
                loadDecisions();
              }}
            />
          )}
          {triggeredAlert && (
            <AlertTriggeredModal
              alert={triggeredAlert.alert}
              decision={triggeredAlert.decision}
              onClose={() => setTriggeredAlert(null)}
              onCloseDecision={(decision, alert) => {
                setTriggeredAlert(null);
                handleCloseDecision(decision.id, decision);
              }}
            />
          )}
          {filteredDecisions.length === 0 && searchTicker === '' && filterType === 'all' && filterStatus === 'all' && !fromDate && !toDate && (
            <Empty text="No decisions yet. Create your first decision to start your journal." />
          )}
          {filteredDecisions.length === 0 && (searchTicker !== '' || filterType !== 'all' || filterStatus !== 'all' || fromDate || toDate) && (
            <Empty text="No decisions match your filters. Try adjusting your search or date range." />
          )}
        </Panel>
      </section>
    </>
  );
}

function PortfolioSummary({ strategyQuotes, selectedStrategy }) {
  if (!strategyQuotes?.symbols || strategyQuotes.symbols.length === 0) {
    return <div style={{ fontSize: '0.9rem', color: '#667085', padding: '12px' }}>Add symbols to see strategy metrics</div>;
  }

  const owned = strategyQuotes.symbols.filter((q) => q.trackingStatus === 'owned');
  const watch = strategyQuotes.symbols.filter((q) => q.trackingStatus === 'watch');
  const startingCapital = strategyQuotes.startingCapital ?? selectedStrategy?.startingCapital ?? 0;
  const totalValue = Number(strategyQuotes.totalStrategyValue ?? startingCapital);
  const cashBalance = Number(strategyQuotes.cashBalance ?? startingCapital);
  const holdingsValue = Number(strategyQuotes.holdingsValue ?? 0);
  const gainLoss = Number(strategyQuotes.totalGain ?? 0);
  const gainLossPercent = Number(strategyQuotes.totalGainPct ?? 0);
  const isPositive = gainLoss >= 0;

  return (
    <div style={{ padding: '12px' }}>
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '12px', marginBottom: '12px' }}>
        <div style={{ background: '#f9fbfb', padding: '10px', borderRadius: '7px', border: '1px solid #e2e8f0' }}>
          <small style={{ color: '#667085', fontSize: '0.75rem' }}>Strategy Value</small>
          <div style={{ fontSize: '1.3rem', fontWeight: 700, color: '#20242a' }}>
            {currencyPrecise(totalValue)}
          </div>
        </div>
        <div style={{ background: '#f9fbfb', padding: '10px', borderRadius: '7px', border: '1px solid #e2e8f0' }}>
          <small style={{ color: '#667085', fontSize: '0.75rem' }}>Cash</small>
          <div style={{ fontSize: '1rem', fontWeight: 600, color: '#526071' }}>
            {currencyPrecise(cashBalance)}
          </div>
        </div>
      </div>
      <div style={{ background: isPositive ? '#f0fdf4' : '#fef2f2', padding: '10px', borderRadius: '7px', border: `1px solid ${isPositive ? '#dcfce7' : '#fee2e2'}` }}>
        <small style={{ color: '#667085', fontSize: '0.75rem' }}>Total Strategy P&L</small>
        <div style={{ fontSize: '1.2rem', fontWeight: 700, color: isPositive ? '#16a34a' : '#dc2626', display: 'flex', justifyContent: 'space-between', alignItems: 'baseline' }}>
          <span>{signedCurrency(gainLoss)}</span>
          <span style={{ fontSize: '0.9rem' }}>{signedNumber(gainLossPercent)}%</span>
        </div>
      </div>
      <div style={{ marginTop: '10px', fontSize: '0.8rem', color: '#667085', paddingTop: '10px', borderTop: '1px solid #e4e7ec' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '4px' }}>
          <span>{owned.length} owned</span>
          <span>{watch.length} watch</span>
          <span>{currencyPrecise(holdingsValue)} invested</span>
        </div>
      </div>
    </div>
  );
}

function AllocationChart({ strategyQuotes }) {
  if (!strategyQuotes?.symbols || strategyQuotes.symbols.length === 0) return null;

  const positions = strategyQuotes.symbols.filter((symbol) => symbol.trackingStatus === 'owned' && symbol.marketValue != null);
  if (positions.length === 0) {
    return <div style={{ fontSize: '0.85rem', color: '#667085', padding: '12px' }}>Record a buy transaction to see allocation.</div>;
  }
  const totalValue = positions.reduce((sum, q) => sum + Number(q.marketValue || 0), 0);
  const allocations = positions.map(s => ({
    symbol: s.symbol,
    value: Number(s.marketValue || 0),
    percent: s.positionWeight != null ? Number(s.positionWeight) : totalValue > 0 ? (Number(s.marketValue || 0) / totalValue) * 100 : 0
  })).sort((a, b) => b.value - a.value);

  const colors = ['#1f6f61', '#0d9488', '#14b8a6', '#2dd4bf', '#67e8f9', '#a5f3fc'];

  return (
    <div style={{ padding: '12px' }}>
      <h3 style={{ margin: '0 0 10px 0', fontSize: '0.95rem', fontWeight: 600 }}>Symbol Allocation</h3>
      <div style={{ display: 'grid', gap: '6px' }}>
        {allocations.map((alloc, idx) => (
          <div key={alloc.symbol} style={{ display: 'grid', gridTemplateColumns: '1fr 3fr 1fr', gap: '8px', alignItems: 'center' }}>
            <strong style={{ fontSize: '0.85rem', color: '#20242a' }}>{alloc.symbol}</strong>
            <div style={{ background: '#e4e7ec', borderRadius: '4px', height: '20px', position: 'relative', overflow: 'hidden' }}>
              <div
                style={{
                  background: colors[idx % colors.length],
                  height: '100%',
                  width: `${alloc.percent}%`,
                  borderRadius: '4px'
                }}
              />
            </div>
            <small style={{ fontSize: '0.75rem', color: '#667085', textAlign: 'right' }}>
              {alloc.percent.toFixed(0)}%
            </small>
          </div>
        ))}
      </div>
    </div>
  );
}

function PriceChart({ strategyHistory, strategyQuotes, selectedHistorySymbol, setSelectedHistorySymbol, chartRange, setChartRange }) {
  const series = strategyHistory?.series || [];
  const performance = strategyHistory?.performance || [];
  const showingStrategy = selectedHistorySymbol === 'strategy' || (!selectedHistorySymbol && performance.length > 0);
  const selectedSeries = showingStrategy ? null : series.find((item) => item.symbol === selectedHistorySymbol) || series[0];
  if (performance.length === 0 && (!selectedSeries?.data || selectedSeries.data.length === 0)) {
    return <div style={{ fontSize: '0.85rem', color: '#667085', padding: '12px' }}>Add owned or watched symbols to view history</div>;
  }

  const ranges = ['1w', '1mo', '3mo', '6mo', '1y'];
  const rawData = showingStrategy ? performance : selectedSeries.data;

  const chartData = rawData.map((point, idx) => {
    const date = new Date(point.timestamp * 1000);
    const isFirstOfYear = idx === 0 || new Date(rawData[idx - 1].timestamp * 1000).getFullYear() !== date.getFullYear();
    const dateStr = isFirstOfYear
      ? date.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: '2-digit' })
      : date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
    const weekday = date.toLocaleDateString('en-US', { weekday: 'short' });
    const day = date.getDate();
    const year = String(date.getFullYear()).slice(-2);
    const tooltipDate = `${weekday} ${day} '${year}`;
    return {
      date: dateStr,
      tooltipDate: tooltipDate,
      timestamp: point.timestamp,
      value: parseFloat(showingStrategy ? point.value : point.close) || 0,
      cash: showingStrategy ? parseFloat(point.cash) || 0 : null,
      holdingsValue: showingStrategy ? parseFloat(point.holdingsValue) || 0 : null,
      returnPct: showingStrategy ? parseFloat(point.returnPct) || 0 : null
    };
  });

  const selectedSymbol = selectedSeries ? strategyQuotes?.symbols?.find((symbol) => symbol.symbol === selectedSeries.symbol) : null;
  const currentValue = showingStrategy
    ? Number(strategyQuotes?.totalStrategyValue ?? chartData.at(-1)?.value ?? 0)
    : Number(selectedSymbol?.lastPrice || selectedSymbol?.price || chartData.at(-1)?.value || 0);
  const startValue = chartData.length > 0 ? chartData[0].value : 0;
  const valueChange = currentValue - startValue;
  const valueChangePercent = startValue > 0 ? (valueChange / startValue) * 100 : 0;
  const isPositive = valueChange >= 0;
  const historyOptions = [
    ...(performance.length ? [{ value: 'strategy', label: 'Total Strategy' }] : []),
    ...series.map((item) => ({ value: item.symbol, label: item.symbol }))
  ];

  return (
    <div style={{ padding: '12px' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline', marginBottom: '12px' }}>
        <div>
          <h3 style={{ margin: '0 0 4px 0', fontSize: '0.95rem', fontWeight: 600 }}>
            {showingStrategy ? 'Strategy Performance' : `${selectedSymbol?.symbol} Price History`}
          </h3>
          <small style={{ color: '#667085', fontSize: '0.75rem' }}>
            {chartData.length} data points
          </small>
        </div>
        <div style={{ textAlign: 'right' }}>
          <div style={{ fontSize: '1.3rem', fontWeight: 700, color: '#20242a' }}>
            ${currentValue.toFixed(2)}
          </div>
          <small style={{ color: isPositive ? '#16a34a' : '#dc2626', fontWeight: 600 }}>
            {isPositive ? '↑' : '↓'} ${Math.abs(valueChange).toFixed(2)} ({isPositive ? '+' : ''}{valueChangePercent.toFixed(2)}%)
          </small>
        </div>
      </div>

      <div style={{ marginBottom: '12px', display: 'flex', gap: '8px', flexWrap: 'wrap', alignItems: 'center', justifyContent: 'space-between' }}>
        <div style={{ display: 'flex', gap: '6px', flexWrap: 'wrap' }}>
          {ranges.map(range => (
            <button
              key={range}
              type="button"
              onClick={() => setChartRange(range)}
              style={{
                padding: '6px 12px',
                fontSize: '0.75rem',
                fontWeight: chartRange === range ? 700 : 500,
                background: chartRange === range ? '#0f766e' : '#f0f0f0',
                color: chartRange === range ? '#fff' : '#526071',
                border: 'none',
                borderRadius: '4px',
                cursor: 'pointer',
                transition: 'all 200ms'
              }}
            >
                {rangeLabel(range)}
            </button>
          ))}
        </div>
        {historyOptions.length > 1 && (
          <label style={{ display: 'flex', alignItems: 'center', gap: '6px', fontSize: '0.75rem', color: '#667085', margin: 0 }}>
            View
            <select
              value={showingStrategy ? 'strategy' : selectedSeries.symbol}
              onChange={(event) => setSelectedHistorySymbol(event.target.value)}
              style={{ minWidth: '132px', padding: '6px 8px', fontSize: '0.8rem' }}
            >
              {historyOptions.map((item) => (
                <option value={item.value} key={item.value}>{item.label}</option>
              ))}
            </select>
          </label>
        )}
      </div>

      <div style={{ background: '#fff', border: '1px solid #e4e7ec', borderRadius: '7px', padding: '8px', marginBottom: '12px' }}>
        <ResponsiveContainer width="100%" height={300}>
          <RechartsLineChart
            data={chartData}
            margin={{ top: 10, right: 20, left: 0, bottom: 20 }}
          >
            <CartesianGrid strokeDasharray="3 3" stroke="#e4e7ec" />
            <XAxis
              dataKey="date"
              stroke="#9facbd"
              style={{ fontSize: '0.75rem' }}
              tick={{ fill: '#9facbd' }}
            />
            <YAxis
              stroke="#9facbd"
              style={{ fontSize: '0.75rem' }}
              tick={{ fill: '#9facbd' }}
              tickFormatter={(value) => Number(value).toFixed(2)}
              domain={['dataMin - 10', 'dataMax + 10']}
            />
            <Tooltip
              contentStyle={{
                background: '#f9fbfb',
                border: '1px solid #e4e7ec',
                borderRadius: '6px',
                fontSize: '0.75rem',
                padding: '8px'
              }}
              formatter={(value) => [`$${Number(value).toFixed(2)}`, showingStrategy ? 'Value' : 'Price']}
              labelFormatter={(label) => {
                const dataPoint = chartData.find(d => d.date === label);
                return dataPoint?.tooltipDate || label;
              }}
              labelStyle={{ color: '#20242a' }}
            />
            <Line
              type="monotone"
              dataKey="value"
              stroke={isPositive ? '#16a34a' : '#dc2626'}
              strokeWidth={2}
              dot={false}
              isAnimationActive={false}
            />
          </RechartsLineChart>
        </ResponsiveContainer>
      </div>

      <div style={{ fontSize: '0.8rem', color: '#667085', paddingTop: '8px', borderTop: '1px solid #e4e7ec' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between' }}>
          <span>Range: {rangeLabel(chartRange)}</span>
          <span>Volatility: {Math.sqrt(chartData.reduce((sum, d, i, arr) => {
            if (i === 0) return 0;
            const prev = arr[i - 1].value;
            const curr = d.value;
            return sum + Math.pow((curr - prev) / prev, 2);
          }, 0) / Math.max(chartData.length - 1, 1)).toFixed(2)}%</span>
        </div>
      </div>
    </div>
  );
}

function PortfolioView({
  strategies,
  selectedStrategyId,
  setSelectedStrategyId,
  selectedStrategy,
  strategyActivities,
  strategyForm,
  setStrategyForm,
  createStrategy,
  updateStrategyVisibility,
  symbolForm,
  setSymbolForm,
  addSymbol,
  removeStrategySymbol,
  startSymbolAction,
  strategyQuotes,
  strategyHistory,
  selectedHistorySymbol,
  setSelectedHistorySymbol,
  expandedIndicatorSymbol,
  toggleSymbolIndicators,
  symbolIndicators,
  chartRange,
  setChartRange,
  showStrategyForm,
  setShowStrategyForm,
  showSymbolForm,
  setShowSymbolForm
}) {
  if (!selectedStrategy) {
    return (
      <section className="workspaceGrid">
        <Panel title="Strategies" icon={<WalletCards />}>
          <StrategyForm strategyForm={strategyForm} setStrategyForm={setStrategyForm} createStrategy={createStrategy} />
          <div className="strategyTabs">
            {strategies.map((strategy) => (
              <button type="button" className={strategy.id === selectedStrategyId ? 'selected' : ''} key={strategy.id} onClick={() => setSelectedStrategyId(strategy.id)}>
                {strategy.name}
              </button>
            ))}
            {strategies.length === 0 && <Empty text="No strategies yet." />}
          </div>
        </Panel>
      </section>
    );
  }

  return (
    <>
      <section className="workspaceGrid strategyWorkspaceTop">
        <Panel title="Strategies" icon={<WalletCards />} action="+" onAction={() => setShowStrategyForm((show) => !show)}>
          <div className="strategyCompactHeader">
            <div style={{ flex: 1 }}>
              <strong style={{ fontSize: '0.95rem' }}>{selectedStrategy.name}</strong>
              <p style={{ margin: '2px 0 0 0', fontSize: '0.8rem', color: '#667085' }}>{selectedStrategy.description}</p>
            </div>
            <button
              type="button"
              className="visibilityToggle"
              onClick={() => updateStrategyVisibility(selectedStrategy.visibility === 'public' ? 'private' : 'public')}
            >
              {selectedStrategy.visibility}
            </button>
          </div>
          {showStrategyForm && (
            <form className="stack compactStrategyForm" onSubmit={(e) => { createStrategy(e); setShowStrategyForm(false); }}>
              <Field label="Strategy Name" value={strategyForm.name} onChange={(value) => setStrategyForm({ ...strategyForm, name: value })} required />
              <TextField label="Description" value={strategyForm.description} onChange={(value) => setStrategyForm({ ...strategyForm, description: value })} required />
              <div className="row">
                <label>
                  Starting Capital
                  <input type="number" min="1" value={strategyForm.startingCapital} onChange={(event) => setStrategyForm({ ...strategyForm, startingCapital: event.target.value })} />
                </label>
                <label>
                  Visibility
                  <select value={strategyForm.visibility} onChange={(event) => setStrategyForm({ ...strategyForm, visibility: event.target.value })}>
                    <option value="private">Private</option>
                    <option value="public">Public</option>
                  </select>
                </label>
              </div>
              <div style={{ display: 'flex', gap: '8px' }}>
                <button className="primary compactActionButton" type="submit"><Plus size={17} />Create</button>
                <button className="secondaryCompactButton" type="button" onClick={() => setShowStrategyForm(false)}>Cancel</button>
              </div>
            </form>
          )}
          <div className="strategyTabs">
            {strategies.map((strategy) => (
              <button type="button" className={strategy.id === selectedStrategyId ? 'selected' : ''} key={strategy.id} onClick={() => setSelectedStrategyId(strategy.id)}>
                {strategy.name}
              </button>
            ))}
          </div>
        </Panel>
        <Panel title="Add Symbol" icon={<Plus />}>
          <form className="symbolForm compactSymbolForm" onSubmit={(e) => { addSymbol(e); setShowSymbolForm(false); }}>
            <label>
              Action
              <select value={symbolForm.action} onChange={(event) => setSymbolForm({ ...symbolForm, action: event.target.value })}>
                <option value="watch">Watch</option>
                <option value="buy">Buy</option>
                <option value="sell">Sell</option>
              </select>
            </label>
            <Field label="Symbol" value={symbolForm.symbol} onChange={(value) => setSymbolForm({ ...symbolForm, symbol: value })} required />
            {symbolForm.action === 'watch' ? (
              <Field label="Note" value={symbolForm.note} onChange={(value) => setSymbolForm({ ...symbolForm, note: value })} />
            ) : (
              <Field label="Shares" value={symbolForm.quantity} onChange={(value) => setSymbolForm({ ...symbolForm, quantity: value })} required />
            )}
            <div style={{ display: 'flex', gap: '6px' }}>
              <button className="primary compactActionButton" type="submit"><Search size={16} />{symbolForm.action === 'watch' ? 'Watch' : 'Record'}</button>
            </div>
          </form>
        </Panel>
      </section>
      <section className="workspaceGrid">
        <Panel title="Strategy History" icon={<LineChart />}>
          <PriceChart
            strategyHistory={strategyHistory}
            strategyQuotes={strategyQuotes}
            selectedHistorySymbol={selectedHistorySymbol}
            setSelectedHistorySymbol={setSelectedHistorySymbol}
            chartRange={chartRange}
            setChartRange={setChartRange}
          />
        </Panel>
        <Panel title="Tracked Symbols" icon={<Compass />}>
          <div className="quoteGrid" style={{ gridTemplateColumns: '1fr' }}>
            {(strategyQuotes?.symbols || []).map((quote) => {
              const change = quote.change || 0;
              const changePercent = quote.percentChange || 0;
              const isPositive = change >= 0;
              const price = quote.lastPrice || quote.price || 0;
              const volume = quote.volume || 0;
              const analysis = symbolIndicators[quote.symbol];
              const isExpanded = expandedIndicatorSymbol === quote.symbol;
              const confidence = analysis?.recommendation?.confidence || 'Low';
              const isMediumOrHigher = confidence === 'Medium' || confidence === 'High';
              const sampleSize = analysis?.recommendation?.sampleSize || 0;
              const hasMedianData = sampleSize > 0 && analysis?.recommendation?.medianReturn !== undefined;
              const shouldShowTrendLabel = isMediumOrHigher || (confidence === 'Low' && hasMedianData);
              const rawLabel = analysis?.recommendation?.label || 'No Data';
              const trendLabel = shouldShowTrendLabel ? rawLabel : 'N/A';
              const trendColor = rawLabel.toLowerCase().includes('bearish') ? '#dc2626'
                                 : rawLabel.toLowerCase().includes('bullish') ? '#16a34a'
                                 : '#9facbd';

              return (
                <article className="quote" key={quote.symbol} style={{ overflow: 'hidden' }}>
                  <div className="symbolQuoteHeader">
                    <div className="symbolQuoteMain">
                      <strong>{quote.symbol}</strong>
                      <span>${price.toFixed(2)}</span>
                    </div>
                    <div className="symbolQuoteAside">
                      <span className={isPositive ? 'positiveText' : 'negativeText'}>
                        {signedNumber(changePercent)}%
                      </span>
                      <div className="symbolActions">
                        {quote.trackingStatus === 'watch' ? (
                          <>
                            <button className="symbolActionButton buy" type="button" onClick={() => startSymbolAction('buy', quote.symbol)}>Buy</button>
                            <button className="symbolActionButton deleteIcon" type="button" title={`Delete ${quote.symbol}`} aria-label={`Delete ${quote.symbol}`} onClick={() => removeStrategySymbol(quote.symbol)}>
                              <MinusCircle size={17} />
                            </button>
                          </>
                        ) : (
                          <button className="symbolActionButton sell" type="button" onClick={() => startSymbolAction('sell', quote.symbol)}>Sell</button>
                        )}
                      </div>
                    </div>
                  </div>
                  <small style={{ color: '#667085', fontSize: '0.75rem', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                    <span>
                      <span className="badge" style={{ marginRight: '6px' }}>{quote.trackingStatus === 'owned' ? 'Owned' : 'Watch'}</span>
                      {quote.trackingStatus === 'owned' && quote.quantity ? `${Number(quote.quantity).toLocaleString()} shares | ` : ''}
                      Change: {signedCurrency(change)}
                      {volume ? ` | Vol: ${(volume / 1e6).toFixed(1)}M` : ''}
                    </span>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '6px', marginLeft: '8px', flexShrink: 0 }}>
                      <span style={{ color: trendColor, fontWeight: 600, minWidth: '75px', textAlign: 'right', fontSize: '0.75rem' }}>
                        {confidence}{shouldShowTrendLabel ? ` • ${trendLabel.substring(0, 8)}` : ''}
                      </span>
                      <button
                        className={`expandIndicatorButton ${isExpanded ? 'expanded' : ''}`}
                        type="button"
                        title={`${isExpanded ? 'Hide' : 'Show'} technical analysis for ${quote.symbol}`}
                        aria-label={`${isExpanded ? 'Hide' : 'Show'} technical analysis for ${quote.symbol}`}
                        onClick={() => toggleSymbolIndicators(quote.symbol)}
                      >
                        {isExpanded ? '−' : '+'}
                      </button>
                    </div>
                  </small>
                  {isExpanded && analysis && (
                    <ExpandedAnalysisPanel symbol={quote.symbol} indicator={analysis} />
                  )}
                </article>
              );
            })}
            {(!strategyQuotes?.symbols || strategyQuotes.symbols.length === 0) && <Empty text="Add a symbol to view prices." />}
          </div>
        </Panel>
      </section>
      <section className="workspaceGrid">
        <Panel title="Strategy Snapshot" icon={<TrendingUp />}>
          <PortfolioSummary strategyQuotes={strategyQuotes} selectedStrategy={selectedStrategy} />
        </Panel>
        <Panel title="Asset Allocation" icon={<BarChart3 />}>
          <AllocationChart strategyQuotes={strategyQuotes} />
        </Panel>
      </section>
      <section className="workspaceGrid">
        <Panel title="Activities" icon={<Activity />} className="span2">
          <div className="activityList">
            {strategyActivities.length > 0 ? (
              strategyActivities.map((activity) => {
                const isBuy = activity.side === 'BUY';
                const date = new Date(activity.executedAt || activity.createdAt);
                return (
                  <article className="activityRow" key={activity.id}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '12px', flex: 1 }}>
                      <div style={{
                        width: '36px',
                        height: '36px',
                        borderRadius: '6px',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        background: isBuy ? '#d4edda' : '#f8d7da',
                        color: isBuy ? '#16a34a' : '#dc2626',
                        fontSize: '0.8rem',
                        fontWeight: 'bold'
                      }}>
                        {isBuy ? '+' : '−'}
                      </div>
                      <div style={{ flex: 1 }}>
                        <div style={{ display: 'flex', gap: '8px', alignItems: 'center' }}>
                          <strong>{activity.ticker}</strong>
                          <span style={{ fontSize: '0.8rem', color: '#667085' }}>
                            {isBuy ? 'Bought' : 'Sold'} {Number(activity.quantity || 0).toLocaleString()} shares
                          </span>
                        </div>
                        <small style={{ color: '#9facbd', fontSize: '0.75rem' }}>
                          @ ${Number(activity.price || 0).toFixed(2)} · {date.toLocaleDateString()} {date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                        </small>
                      </div>
                    </div>
                    {activity.decisionId && (
                      <span style={{ fontSize: '0.75rem', color: '#667085', padding: '4px 8px', background: '#f0f4f8', borderRadius: '4px' }}>
                        Linked decision
                      </span>
                    )}
                  </article>
                );
              })
            ) : (
              <Empty text="No activities yet. Add symbols and transactions to see activity here." />
            )}
          </div>
        </Panel>
      </section>
    </>
  );
}

function InvestmentWorkspaceView({
  portfolioSummary,
  accountForm,
  setAccountForm,
  createAccount,
  holdingForm,
  setHoldingForm,
  addHolding,
  deleteHolding,
  deleteAccount
}) {
  const accounts = portfolioSummary?.accounts || [];
  const [managementOpen, setManagementOpen] = useState(accounts.length === 0);
  const selectedAccount = holdingForm.accountId || (accounts[0]?.id ? String(accounts[0].id) : '');
  const holdings = accounts.flatMap((account) =>
    (account.holdings || []).map((holding) => ({ ...holding, accountId: account.id, accountName: account.name }))
  );
  const gainClass = (amount, percent) => Number(amount ?? percent ?? 0) >= 0 ? 'positiveText' : 'negativeText';

  useEffect(() => {
    if (!holdingForm.accountId && accounts[0]?.id) {
      setHoldingForm((current) => ({ ...current, accountId: String(accounts[0].id) }));
    }
  }, [accounts.length]);

  useEffect(() => {
    if (accounts.length === 0) {
      setManagementOpen(true);
    }
  }, [accounts.length]);

  return (
    <>
      <section className="scoreGrid">
        <Metric icon={<BriefcaseBusiness />} label="Market Value" value={currencyPrecise(portfolioSummary?.totalValue)} detail="Across investment accounts" />
        <Metric icon={<TrendingUp />} label="Unrealized Gain" value={currencyPrecise(portfolioSummary?.totalGain)} detail={percentLabel(portfolioSummary?.totalGainPct)} />
        <Metric icon={<Activity />} label="Daily Gain" value={currencyPrecise(portfolioSummary?.dailyGain)} detail={percentLabel(portfolioSummary?.dailyGainPct)} />
        <Metric icon={<WalletCards />} label="Accounts" value={accounts.length} detail={`${holdings.length} holdings`} />
      </section>

      <section className="workspaceGrid">
        <Panel title="Holdings" icon={<BarChart3 />} className="span2">
          <div className="holdingTable">
            <div className="holdingHeader">
              <span>Symbol</span>
              <span>Account</span>
              <span>Shares</span>
              <span>Price</span>
              <span>Value</span>
              <span>Daily G/L</span>
              <span>Total G/L</span>
              <span></span>
            </div>
            {holdings.map((holding) => (
              <article className="holdingRow" key={holding.id}>
                <strong>{holding.symbol}</strong>
                <span>{holding.accountName}</span>
                <span>{Number(holding.shares || 0).toLocaleString()}</span>
                <span>{currencyPrecise(holding.price)} {holding.manualPriceActive && <small className="manualBadge">M</small>}</span>
                <span>{currencyPrecise(holding.value)}</span>
                <span className={`gainStack ${gainClass(holding.dayGain, holding.dayChangePct)}`}>
                  <strong>{currencyPrecise(holding.dayGain)}</strong>
                  <small>{signedPercentLabel(holding.dayChangePct)}</small>
                </span>
                <span className={`gainStack ${gainClass(holding.gain, holding.gainPct)}`}>
                  <strong>{currencyPrecise(holding.gain)}</strong>
                  <small>{signedPercentLabel(holding.gainPct)}</small>
                </span>
                <button type="button" onClick={() => deleteHolding(holding.accountId, holding.id, holding)}>Remove</button>
              </article>
            ))}
            {holdings.length === 0 && <Empty text="No holdings yet." />}
          </div>
        </Panel>
      </section>

      <section className="workspaceGrid">
        <Panel
          title="Manage Accounts and Holdings"
          icon={<BriefcaseBusiness />}
          className="span2"
          action={managementOpen ? 'Collapse' : 'Open'}
          onAction={() => setManagementOpen((open) => !open)}
        >
          {managementOpen && (
            <div className="managementGrid">
              <div>
                <h3><BriefcaseBusiness size={16} />Investment Accounts</h3>
                <form className="stack" onSubmit={createAccount}>
                  <div className="row">
                    <Field label="Account Name" value={accountForm.name} onChange={(value) => setAccountForm({ ...accountForm, name: value })} required />
                    <label>
                      Type
                      <select value={accountForm.accountType} onChange={(event) => setAccountForm({ ...accountForm, accountType: event.target.value })}>
                        <option value="BROKERAGE">Brokerage</option>
                        <option value="IRA">IRA</option>
                        <option value="ROTH_IRA">Roth IRA</option>
                        <option value="FOUR_O_ONE_K">401K</option>
                        <option value="HSA">HSA</option>
                      </select>
                    </label>
                  </div>
                  <button className="primary" type="submit"><Plus size={17} />Create Account</button>
                </form>
                <div className="accountList">
                  {accounts.map((account) => (
                    <article className="accountRow" key={account.id}>
                      <div>
                        <strong>{account.name}</strong>
                        <small>{accountTypeLabel(account.accountType)} · {currencyPrecise(account.value)} value</small>
                      </div>
                      <button type="button" onClick={() => deleteAccount(account.id)}>Delete</button>
                    </article>
                  ))}
                  {accounts.length === 0 && <Empty text="Create an account before adding holdings." />}
                </div>
              </div>

              <div>
                <h3><Plus size={16} />Add Holding</h3>
                <form className="stack" onSubmit={addHolding}>
                  <label>
                    Account
                    <select
                      value={String(selectedAccount)}
                      onChange={(event) => setHoldingForm({ ...holdingForm, accountId: event.target.value })}
                      disabled={accounts.length === 0}
                    >
                      {accounts.map((account) => (
                        <option key={account.id} value={account.id}>{account.name}</option>
                      ))}
                    </select>
                  </label>
                  <div className="row">
                    <Field label="Symbol" value={holdingForm.symbol} onChange={(value) => setHoldingForm({ ...holdingForm, symbol: value })} required />
                    <Field label="Shares" value={holdingForm.shares} onChange={(value) => setHoldingForm({ ...holdingForm, shares: value })} required />
                  </div>
                  <div className="row">
                    <Field label="Cost Basis" value={holdingForm.costBasis} onChange={(value) => setHoldingForm({ ...holdingForm, costBasis: value })} />
                    <Field label="Manual Price" value={holdingForm.manualPrice} onChange={(value) => setHoldingForm({ ...holdingForm, manualPrice: value })} />
                  </div>
                  <label>
                    Purchase Date
                    <input type="date" value={holdingForm.purchaseDate} onChange={(event) => setHoldingForm({ ...holdingForm, purchaseDate: event.target.value })} />
                  </label>
                  <button className="primary" type="submit" disabled={accounts.length === 0}><Plus size={17} />Add Holding</button>
                </form>
              </div>
            </div>
          )}
        </Panel>
      </section>
    </>
  );
}

function ReviewsView({ reviews, pendingReviews, overdueReviews }) {
  return (
    <section className="workspaceGrid">
      <Panel title="Review Queue" icon={<CheckCircle2 />}>
        <div className="analytics">
          <Score label="Pending" value={pendingReviews.length} />
          <Score label="Overdue" value={overdueReviews.length} />
        </div>
        <div className="list">
          {reviews.map((review) => (
            <article className="listItem" key={review.id}>
              <strong>{review.reviewType}</strong>
              <span>Decision #{review.decisionId}</span>
              <small>{review.status} · due {review.dueDate}</small>
            </article>
          ))}
          {reviews.length === 0 && <Empty text="No scheduled reviews yet." />}
        </div>
      </Panel>
    </section>
  );
}

function AnalyticsView({ dqs, behavior, decisions, reviews }) {
  return (
    <section className="workspaceGrid">
      <Panel title="Score Components" icon={<Brain />}>
        <div className="analytics">
          <Score label="DQS" value={dqs?.score ?? 0} />
          <Score label="Behavior" value={behavior?.behavioralScore ?? 0} />
          <Score label="Decisions" value={decisions.length} />
          <Score label="Reviews" value={reviews.length} />
        </div>
        <div className="componentGrid">
          {dqs?.components && Object.entries(dqs.components).map(([name, component]) => (
            <div className="component" key={name}>
              <span>{labelize(name)}</span>
              <strong>{component.score}</strong>
              <small>{Math.round(component.weight * 100)}% weight</small>
            </div>
          ))}
        </div>
      </Panel>
      <Panel title="Behavior Insights" icon={<Activity />}>
        <div className="list">
          {(behavior?.insights || []).map((insight) => (
            <article className="listItem" key={insight.title}>
              <strong>{insight.title}</strong>
              <span>{insight.detail}</span>
              <small>{insight.type}</small>
            </article>
          ))}
        </div>
      </Panel>
    </section>
  );
}

function CommunityView({ publicProfile, strategies, ownerId }) {
  const publicStrategies = strategies.filter((strategy) => strategy.visibility === 'public');
  const communityProfiles = generateMockCommunityProfiles(publicProfile);
  const leaderboard = communityProfiles.slice().sort((a, b) => (b.reputation?.decisionQualityScore || 0) - (a.reputation?.decisionQualityScore || 0)).slice(0, 5);

  return (
    <>
      <section className="workspaceGrid">
        <Panel title="Your Community Presence" icon={<Users />}>
          {publicProfile ? (
            <div className="communityCard">
              <strong>@{publicProfile.handle}</strong>
              <span>{publicStrategies.length} {publicStrategies.length === 1 ? 'strategy' : 'strategies'} public</span>
              <small style={{ marginTop: '8px', display: 'block', color: '#667085' }}>{publicProfile.bio || 'No bio yet'}</small>
            </div>
          ) : (
            <div style={{ padding: '16px', background: '#f9fbfb', borderRadius: '7px', border: '1px solid #e4e7ec' }}>
              <p style={{ margin: '0 0 12px 0', color: '#526071' }}>Publish your profile to join the community and be discovered by other investors.</p>
              <p style={{ margin: '0', fontSize: '0.85rem', color: '#9facbd' }}>Share your public strategies, reputation scores, and investment thesis with the community.</p>
            </div>
          )}
        </Panel>

        <Panel title="Top Investors by DQS" icon={<TrendingUp />}>
          <div className="list compact">
            {leaderboard.map((profile, idx) => (
              <article className="listItem" key={profile.handle} style={{ display: 'grid', gridTemplateColumns: 'auto 1fr auto', gap: '12px', alignItems: 'center' }}>
                <strong style={{ fontSize: '1.2rem', color: '#9facbd', minWidth: '24px' }}>#{idx + 1}</strong>
                <div>
                  <strong>{profile.displayName}</strong>
                  <span style={{ display: 'block', fontSize: '0.8rem', color: '#667085' }}>@{profile.handle}</span>
                </div>
                <div style={{ textAlign: 'right' }}>
                  <strong style={{ display: 'block', fontSize: '1.1rem', color: '#20242a' }}>{profile.reputation?.decisionQualityScore ?? 'N/A'}</strong>
                  <small style={{ color: '#9facbd' }}>DQS</small>
                </div>
              </article>
            ))}
          </div>
        </Panel>
      </section>

      <section className="workspaceGrid">
        <Panel title="Featured Public Strategies" icon={<WalletCards />}>
          <div className="strategyCards">
            {publicStrategies.slice(0, 3).map((strategy) => (
              <article className="miniCard" key={strategy.id}>
                <strong>{strategy.name}</strong>
                <span>{currency(strategy.startingCapital)}</span>
                <small>public research surface</small>
              </article>
            ))}
            {publicStrategies.length === 0 && <Empty text="No public strategies yet in the community." />}
          </div>
        </Panel>

        <Panel title="Browse Investors" icon={<Compass />}>
          <div className="list compact">
            {communityProfiles.slice(0, 4).map((profile) => (
              <article className="listItem" key={profile.handle}>
                <strong>{profile.displayName}</strong>
                <span style={{ fontSize: '0.85rem', color: '#667085' }}>{profile.bio}</span>
                <small>{profile.publishedStrategies.length} public {profile.publishedStrategies.length === 1 ? 'strategy' : 'strategies'}</small>
              </article>
            ))}
          </div>
        </Panel>
      </section>
    </>
  );
}

function ProfileView({ publicProfile, profileForm, setProfileForm, toggleMetric, publishProfile }) {
  return (
    <section className="workspaceGrid">
      <Panel title="Public Profile" icon={<UserRound />}>
        <form className="stack" onSubmit={publishProfile}>
          <div className="row">
            <Field label="Handle" value={profileForm.handle} onChange={(value) => setProfileForm({ ...profileForm, handle: value })} required />
            <Field label="Display Name" value={profileForm.displayName} onChange={(value) => setProfileForm({ ...profileForm, displayName: value })} required />
          </div>
          <TextField label="Bio" value={profileForm.bio} onChange={(value) => setProfileForm({ ...profileForm, bio: value })} />
          <div className="metricChoices">
            {[
              ['dqs', 'DQS'],
              ['researchDiscipline', 'Research'],
              ['riskManagement', 'Risk'],
              ['strategyConsistency', 'Consistency']
            ].map(([id, label]) => (
              <label className="checkChoice" key={id}>
                <input type="checkbox" checked={profileForm.publishedMetricIds.includes(id)} onChange={() => toggleMetric(id)} />
                {label}
              </label>
            ))}
          </div>
          <button className="primary" type="submit"><ShieldCheck size={17} />Publish Profile</button>
        </form>
      </Panel>
      <Panel title="Public Preview" icon={<ShieldCheck />}>
        {publicProfile ? (
          <div className="publicPreview">
            <div className="strategyHeader">
              <div>
                <h3>{publicProfile.displayName}</h3>
                <p>/public/investors/{publicProfile.handle}</p>
              </div>
              <span className="badge">public</span>
            </div>
            <div className="analytics">
              <Score label="DQS" value={publicProfile.reputation.decisionQualityScore ?? '-'} />
              <Score label="Research" value={publicProfile.reputation.researchDiscipline ?? '-'} />
              <Score label="Risk" value={publicProfile.reputation.riskManagement ?? '-'} />
              <Score label="Consistency" value={publicProfile.reputation.strategyConsistency ?? '-'} />
            </div>
          </div>
        ) : <Empty text="Publish a profile to preview public reputation signals." />}
      </Panel>
    </section>
  );
}

function SettingsView({ ownerId, authToken, demoMode }) {
  return (
    <section className="workspaceGrid">
      <Panel title="Local Development Settings" icon={<Settings />}>
        <div className="listItem">
          <strong>{authToken ? 'Authenticated identity' : 'Demo identity'}</strong>
          <span>{ownerId}</span>
          <small>{authToken ? 'The local UI is using an issued backend bearer token.' : demoMode ? 'The local UI is using the explicit demo workspace.' : 'Sign in or continue as demo to load a workspace.'}</small>
        </div>
      </Panel>
    </section>
  );
}

function DecisionForm({ decisionForm, setDecisionForm, createDecision }) {
  const typeDescriptions = {
    watch: 'Monitor for future action',
    buy: 'Initiate or increase position',
    sell: 'Close or reduce position',
    avoid: 'Do not engage with'
  };

  return (
    <form className="stack" onSubmit={createDecision}>
      <div className="row">
        <Field label="Ticker" value={decisionForm.ticker} onChange={(value) => setDecisionForm({ ...decisionForm, ticker: value })} placeholder="e.g., AAPL, MSFT" required />
        <div>
          <label style={{ display: 'grid', gap: '6px' }}>
            Type
            <select value={decisionForm.decisionType} onChange={(event) => setDecisionForm({ ...decisionForm, decisionType: event.target.value })} style={{ width: '100%' }}>
              <option value="watch">Watch</option>
              <option value="buy">Buy</option>
              <option value="sell">Sell</option>
              <option value="avoid">Avoid</option>
            </select>
          </label>
          <small style={{ display: 'block', marginTop: '4px', color: '#667085', paddingLeft: '2px' }}>{typeDescriptions[decisionForm.decisionType]}</small>
        </div>
      </div>
      <Field label="Title" value={decisionForm.title} onChange={(value) => setDecisionForm({ ...decisionForm, title: value })} placeholder="Brief decision summary" required />
      <TextField label="Thesis" value={decisionForm.thesis} onChange={(value) => setDecisionForm({ ...decisionForm, thesis: value })} placeholder="Why are you making this decision? What's your investment case?" required />
      <TextField label="Evidence" value={decisionForm.evidence} onChange={(value) => setDecisionForm({ ...decisionForm, evidence: value })} placeholder="Supporting facts, metrics, or research. One per line." required />
      <TextField label="Risks" value={decisionForm.riskFactors} onChange={(value) => setDecisionForm({ ...decisionForm, riskFactors: value })} placeholder="What could go wrong? List key downside scenarios. One per line." required />
      <div className="row">
        <div>
          <label style={{ display: 'grid', gap: '6px' }}>
            Confidence (1-10)
            <input type="number" min="1" max="10" value={decisionForm.confidence} onChange={(event) => setDecisionForm({ ...decisionForm, confidence: event.target.value })} style={{ width: '100%' }} />
          </label>
          <small style={{ display: 'block', marginTop: '4px', color: '#667085', paddingLeft: '2px' }}>How confident are you in this decision?</small>
        </div>
        <Field label="Time Horizon" value={decisionForm.timeHorizon} onChange={(value) => setDecisionForm({ ...decisionForm, timeHorizon: value })} placeholder="e.g., 3 months, 1 year" required />
      </div>
      <TextField label="Exit Criteria" value={decisionForm.exitCriteria} onChange={(value) => setDecisionForm({ ...decisionForm, exitCriteria: value })} placeholder="When will you exit? What conditions trigger sale or review? One per line." required />
      <button className="primary" type="submit"><Plus size={17} />Create Decision</button>
    </form>
  );
}

function StrategyForm({ strategyForm, setStrategyForm, createStrategy }) {
  return (
    <form className="stack" onSubmit={createStrategy}>
      <Field label="Strategy Name" value={strategyForm.name} onChange={(value) => setStrategyForm({ ...strategyForm, name: value })} required />
      <TextField label="Description" value={strategyForm.description} onChange={(value) => setStrategyForm({ ...strategyForm, description: value })} required />
      <div className="row">
        <label>
          Starting Capital
          <input type="number" min="1" value={strategyForm.startingCapital} onChange={(event) => setStrategyForm({ ...strategyForm, startingCapital: event.target.value })} />
        </label>
        <label>
          Visibility
          <select value={strategyForm.visibility} onChange={(event) => setStrategyForm({ ...strategyForm, visibility: event.target.value })}>
            <option value="private">Private</option>
            <option value="public">Public</option>
          </select>
        </label>
      </div>
      <button className="primary" type="submit"><Plus size={17} />Create Strategy</button>
    </form>
  );
}

function ExpandedAnalysisPanel({ symbol, indicator }) {
  const [activeTab, setActiveTab] = useState('chart');
  const [selectedIndicators, setSelectedIndicators] = useState({
    sma20: true,
    sma50: false,
    rsi: false,
    macd: false
  });

  const toggleIndicator = (indicatorName) => {
    setSelectedIndicators(prev => ({
      ...prev,
      [indicatorName]: !prev[indicatorName]
    }));
  };

  return (
    <div style={{ marginTop: '10px', paddingTop: '10px', borderTop: '1px solid #e4e7ec' }}>
      <div style={{ display: 'flex', gap: '12px', marginBottom: '12px', borderBottom: '1px solid #e4e7ec' }}>
        <button
          type="button"
          onClick={() => setActiveTab('chart')}
          style={{
            background: 'none',
            border: 'none',
            padding: '8px 0',
            fontSize: '0.85rem',
            fontWeight: activeTab === 'chart' ? 700 : 500,
            color: activeTab === 'chart' ? '#2563eb' : '#9facbd',
            cursor: 'pointer',
            borderBottom: activeTab === 'chart' ? '2px solid #2563eb' : 'none',
            marginBottom: '-1px'
          }}
        >
          Price Chart & Indicators
        </button>
        <button
          type="button"
          onClick={() => setActiveTab('analysis')}
          style={{
            background: 'none',
            border: 'none',
            padding: '8px 0',
            fontSize: '0.85rem',
            fontWeight: activeTab === 'analysis' ? 700 : 500,
            color: activeTab === 'analysis' ? '#2563eb' : '#9facbd',
            cursor: 'pointer',
            borderBottom: activeTab === 'analysis' ? '2px solid #2563eb' : 'none',
            marginBottom: '-1px'
          }}
        >
          Recommendation Details
        </button>
      </div>

      {activeTab === 'chart' && (
        <TechnicalChartPanel symbol={symbol} indicator={indicator} selectedIndicators={selectedIndicators} toggleIndicator={toggleIndicator} />
      )}

      {activeTab === 'analysis' && (
        <IndicatorPanel symbol={symbol} indicator={indicator} compact={true} />
      )}
    </div>
  );
}

function TechnicalChartPanel({ symbol, indicator, selectedIndicators, toggleIndicator }) {
  if (!indicator || !symbol) return <div style={{ fontSize: '0.85rem', color: '#667085' }}>Loading chart...</div>;

  const analysis = indicator.recommendation;
  const indicatorsData = indicator.indicators;
  const closes = indicatorsData?.closes || [];
  const similarSetups = analysis?.similarSetups || [];
  const sma20 = indicatorsData?.sma20 || [];
  const sma50 = indicatorsData?.sma50 || [];
  const rsi = indicatorsData?.rsi || [];
  const macdLine = indicatorsData?.macdLine || [];

  if (closes.length === 0) {
    return <div style={{ fontSize: '0.85rem', color: '#667085', padding: '20px', textAlign: 'center' }}>No price data available</div>;
  }

  const chartData = closes.map((close, idx) => {
    const setup = similarSetups.find(s => s.idx === idx);
    const date = new Date(0);
    date.setUTCSeconds(Math.floor(idx * 86400));
    return {
      idx,
      price: parseFloat(close),
      sma20: sma20[idx] ? parseFloat(sma20[idx]) : null,
      sma50: sma50[idx] ? parseFloat(sma50[idx]) : null,
      rsi: rsi[idx] ? parseFloat(rsi[idx]) : null,
      macd: macdLine[idx] ? parseFloat(macdLine[idx]) : null,
      date: date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' }),
      similarSetup: setup ? {
        returnPct: setup.forwardReturn,
        direction: setup.direction
      } : null
    };
  });

  const CustomDot = (props) => {
    const { cx, cy, payload } = props;
    if (!payload.similarSetup) return null;
    const returnColor = payload.similarSetup.returnPct > 0 ? '#16a34a' : '#dc2626';
    return (
      <g>
        <circle cx={cx} cy={cy} r={5} fill={returnColor} opacity={0.7} />
        <circle cx={cx} cy={cy} r={8} fill="none" stroke={returnColor} strokeWidth={2} opacity={0.5} />
      </g>
    );
  };

  const minPrice = Math.min(...closes.map(v => parseFloat(v)));
  const maxPrice = Math.max(...closes.map(v => parseFloat(v)));
  const priceRange = maxPrice - minPrice || 1;

  return (
    <div style={{ width: '100%', display: 'flex', flexDirection: 'column', gap: '12px' }}>
      <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
        <button
          type="button"
          onClick={() => toggleIndicator('sma20')}
          style={{
            padding: '4px 12px',
            fontSize: '0.75rem',
            border: `1px solid ${selectedIndicators.sma20 ? '#2563eb' : '#e4e7ec'}`,
            background: selectedIndicators.sma20 ? '#2563eb' : '#fff',
            color: selectedIndicators.sma20 ? '#fff' : '#667085',
            borderRadius: '3px',
            cursor: 'pointer',
            fontWeight: 500
          }}
        >
          SMA20
        </button>
        <button
          type="button"
          onClick={() => toggleIndicator('sma50')}
          style={{
            padding: '4px 12px',
            fontSize: '0.75rem',
            border: `1px solid ${selectedIndicators.sma50 ? '#f97316' : '#e4e7ec'}`,
            background: selectedIndicators.sma50 ? '#f97316' : '#fff',
            color: selectedIndicators.sma50 ? '#fff' : '#667085',
            borderRadius: '3px',
            cursor: 'pointer',
            fontWeight: 500
          }}
        >
          SMA50
        </button>
        <button
          type="button"
          onClick={() => toggleIndicator('rsi')}
          style={{
            padding: '4px 12px',
            fontSize: '0.75rem',
            border: `1px solid ${selectedIndicators.rsi ? '#8b5cf6' : '#e4e7ec'}`,
            background: selectedIndicators.rsi ? '#8b5cf6' : '#fff',
            color: selectedIndicators.rsi ? '#fff' : '#667085',
            borderRadius: '3px',
            cursor: 'pointer',
            fontWeight: 500
          }}
        >
          RSI
        </button>
        <button
          type="button"
          onClick={() => toggleIndicator('macd')}
          style={{
            padding: '4px 12px',
            fontSize: '0.75rem',
            border: `1px solid ${selectedIndicators.macd ? '#ec4899' : '#e4e7ec'}`,
            background: selectedIndicators.macd ? '#ec4899' : '#fff',
            color: selectedIndicators.macd ? '#fff' : '#667085',
            borderRadius: '3px',
            cursor: 'pointer',
            fontWeight: 500
          }}
        >
          MACD
        </button>
      </div>

      <div style={{ height: '300px' }}>
        <ResponsiveContainer width="100%" height={250}>
          <RechartsLineChart data={chartData} margin={{ top: 5, right: 30, left: 0, bottom: 5 }}>
            <CartesianGrid strokeDasharray="3 3" stroke="#e4e7ec" />
            <XAxis
              dataKey="date"
              tick={{ fontSize: 12, fill: '#9facbd' }}
              stroke="#9facbd"
              interval={Math.floor(chartData.length / 8)}
            />
            <YAxis
              stroke="#9facbd"
              domain={[minPrice - priceRange * 0.05, maxPrice + priceRange * 0.05]}
              width={50}
              tickFormatter={(v) => `$${v.toFixed(0)}`}
            />
            <Tooltip
              contentStyle={{ background: '#fff', border: '1px solid #e4e7ec', borderRadius: '4px', fontSize: '0.75rem' }}
              formatter={(value, name) => {
                if (!value) return '';
                if (name === 'price') return [`$${parseFloat(value).toFixed(2)}`, 'Close'];
                if (name === 'sma20') return [`$${parseFloat(value).toFixed(2)}`, 'SMA20'];
                if (name === 'sma50') return [`$${parseFloat(value).toFixed(2)}`, 'SMA50'];
                if (name === 'rsi') return [parseFloat(value).toFixed(1), 'RSI'];
                if (name === 'macd') return [parseFloat(value).toFixed(2), 'MACD'];
                return [value, name];
              }}
            />
            <Line
              type="monotone"
              dataKey="price"
              stroke="#2563eb"
              dot={<CustomDot />}
              strokeWidth={2}
              isAnimationActive={false}
            />
            {selectedIndicators.sma20 && (
              <Line
                type="monotone"
                dataKey="sma20"
                stroke="#2563eb"
                strokeWidth={1.5}
                strokeDasharray="5 5"
                dot={false}
                isAnimationActive={false}
              />
            )}
            {selectedIndicators.sma50 && (
              <Line
                type="monotone"
                dataKey="sma50"
                stroke="#f97316"
                strokeWidth={1.5}
                strokeDasharray="5 5"
                dot={false}
                isAnimationActive={false}
              />
            )}
            {selectedIndicators.rsi && (
              <Line
                type="monotone"
                dataKey="rsi"
                stroke="#8b5cf6"
                strokeWidth={1.5}
                strokeDasharray="5 5"
                dot={false}
                isAnimationActive={false}
                yAxisId="rsiAxis"
              />
            )}
            {selectedIndicators.macd && (
              <Line
                type="monotone"
                dataKey="macd"
                stroke="#ec4899"
                strokeWidth={1.5}
                strokeDasharray="5 5"
                dot={false}
                isAnimationActive={false}
                yAxisId="macdAxis"
              />
            )}
            {(selectedIndicators.rsi || selectedIndicators.macd) && (
              <>
                {selectedIndicators.rsi && <YAxis yAxisId="rsiAxis" orientation="right" stroke="#8b5cf6" width={40} domain={[0, 100]} />}
                {selectedIndicators.macd && <YAxis yAxisId="macdAxis" orientation="right" stroke="#ec4899" width={40} />}
              </>
            )}
          </RechartsLineChart>
        </ResponsiveContainer>
      </div>

      {similarSetups.length > 0 && (
        <div style={{ padding: '8px 0', borderTop: '1px solid #e4e7ec', fontSize: '0.75rem', color: '#667085' }}>
          <strong style={{ color: '#20242a' }}>Similar Setup Points:</strong>
          <div style={{ display: 'flex', gap: '12px', marginTop: '6px', flexWrap: 'wrap' }}>
            {similarSetups.slice(0, 8).map((setup, idx) => (
              <div key={idx} style={{
                padding: '4px 8px',
                background: setup.forwardReturn > 0 ? '#f0fdf4' : '#fef2f2',
                border: `1px solid ${setup.forwardReturn > 0 ? '#86efac' : '#fca5a5'}`,
                borderRadius: '3px'
              }}>
                <small style={{ fontWeight: 600, color: setup.forwardReturn > 0 ? '#166534' : '#991b1b' }}>
                  {setup.forwardReturn > 0 ? '+' : ''}{setup.forwardReturn.toFixed(2)}%
                </small>
              </div>
            ))}
            {similarSetups.length > 8 && (
              <div style={{ padding: '4px 8px', color: '#9facbd' }}>
                +{similarSetups.length - 8} more
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}

function IndicatorPanel({ symbol, indicator, compact = false }) {
  if (!indicator || !symbol) return <div style={{ fontSize: '0.85rem', color: '#667085' }}>Select a symbol for indicators</div>;

  const analysis = indicator.recommendation;
  const indicators = indicator.indicators;

  let rsi = 0;
  let rsiStatus = 'N/A';
  let rsiColor = '#9facbd';

  if (indicators?.rsi && indicators.rsi.length > 0) {
    const rsiValues = indicators.rsi.filter(v => v !== null);
    rsi = rsiValues.length > 0 ? rsiValues[rsiValues.length - 1] : 0;
    rsiStatus = rsi > 70 ? 'Overbought' : rsi < 30 ? 'Oversold' : 'Neutral';
    rsiColor = rsi > 70 ? '#dc2626' : rsi < 30 ? '#16a34a' : '#9facbd';
  }

  const confidence = analysis?.confidence || 'Low';
  const isMediumOrHigher = confidence === 'Medium' || confidence === 'High';

  // Show bull/bear if confidence >= Medium OR if Low confidence with median return data
  const hasMedianData = analysis?.sampleSize > 0 && analysis?.medianReturn !== undefined;
  const shouldShowTrend = isMediumOrHigher || (confidence === 'Low' && hasMedianData);

  const trendLabel = shouldShowTrend ? (analysis?.label || 'No Validated Edge') : 'No Validated Edge';
  const isValidated = !trendLabel.includes('not') && !trendLabel.includes('limited');
  const trendColor = trendLabel.toLowerCase().includes('bearish') ? '#dc2626'
                     : trendLabel.toLowerCase().includes('bullish') ? '#16a34a'
                     : '#9facbd';

  if (compact) {
    return (
      <div style={{ fontSize: '0.75rem', display: 'grid', gap: '8px' }}>
        {shouldShowTrend && (
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <strong style={{ color: trendColor, fontSize: '0.85rem' }}>
              {trendLabel}
            </strong>
            <span style={{ background: confidence === 'High' ? '#dcfce7' : confidence === 'Medium' ? '#fef3c7' : '#fee2e2',
                           color: confidence === 'High' ? '#166534' : confidence === 'Medium' ? '#92400e' : '#991b1b',
                           padding: '2px 6px', borderRadius: '3px', fontSize: '0.7rem', fontWeight: 600 }}>
              {confidence}
            </span>
          </div>
        )}
        {!shouldShowTrend && (
          <div style={{ padding: '4px 8px', background: '#fee2e2', border: '1px solid #fca5a5', borderRadius: '3px', textAlign: 'center' }}>
            <small style={{ color: '#991b1b', fontWeight: 600 }}>No Recommendation</small>
          </div>
        )}
        {analysis?.signals && analysis.signals.length > 0 && (
          <div style={{ fontSize: '0.7rem', color: '#667085' }}>
            {analysis.signals.map((sig, idx) => (
              <div key={idx} style={{ display: 'flex', justifyContent: 'space-between' }}>
                <span>{sig.indicator}</span>
                <strong style={{ color: sig.signal === 'Buy' ? '#16a34a' : '#dc2626' }}>{sig.signal}</strong>
              </div>
            ))}
          </div>
        )}
        {analysis?.sampleSize > 0 && (
          <small style={{ color: '#9facbd', marginTop: '4px' }}>
            {analysis.sampleSize} setups • {(analysis.winRate || 0).toFixed(0)}% win • {(analysis.medianReturn || 0).toFixed(1)}% ret
          </small>
        )}
      </div>
    );
  }

  return (
    <div style={{ fontSize: '0.8rem' }}>
      <div style={{ marginBottom: '12px', paddingBottom: '10px', borderBottom: '2px solid #e4e7ec' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '8px' }}>
          <div>
            <strong style={{ fontSize: '1rem', color: trendColor, display: 'block' }}>
              {trendLabel}
            </strong>
            {analysis?.strategy && analysis.strategy !== 'N/A' && (
              <small style={{ color: '#667085', display: 'block', marginTop: '2px' }}>
                {analysis.strategy}
              </small>
            )}
          </div>
          <span style={{ background: confidence === 'High' ? '#dcfce7' : confidence === 'Medium' ? '#fef3c7' : '#fee2e2',
                         color: confidence === 'High' ? '#166534' : confidence === 'Medium' ? '#92400e' : '#991b1b',
                         padding: '4px 8px', borderRadius: '4px', fontSize: '0.75rem', fontWeight: 700, minWidth: '50px', textAlign: 'center' }}>
            {confidence}
          </span>
        </div>
        {analysis?.reason && (
          <small style={{ color: '#667085', display: 'block' }}>
            {analysis.reason}
          </small>
        )}
      </div>

      {analysis?.signals && analysis.signals.length > 0 && (
        <div style={{ marginBottom: '10px', paddingBottom: '10px', borderBottom: '1px solid #e4e7ec' }}>
          <strong style={{ display: 'block', marginBottom: '6px', color: '#20242a' }}>Trend Inputs</strong>
          {analysis.signals.map((sig, idx) => (
            <div key={idx} style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '4px', alignItems: 'center' }}>
              <span style={{ color: '#667085' }}>{sig.indicator}</span>
              <div>
                <strong style={{ color: sig.signal === 'Buy' ? '#16a34a' : '#dc2626', marginRight: '8px', minWidth: '40px', textAlign: 'right', display: 'inline-block' }}>
                  {sig.signal}
                </strong>
                <small style={{ color: '#9facbd' }}>{sig.detail}</small>
              </div>
            </div>
          ))}
        </div>
      )}

      {analysis?.sampleSize && analysis.sampleSize > 0 && (
        <div style={{ marginBottom: '10px', paddingBottom: '10px', borderBottom: '1px solid #e4e7ec' }}>
          <strong style={{ display: 'block', marginBottom: '6px', color: '#20242a' }}>Historical Validation</strong>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px' }}>
            <div>
              <small style={{ color: '#667085', display: 'block' }}>Similar Setups</small>
              <strong style={{ fontSize: '0.95rem', color: '#20242a' }}>{analysis.sampleSize}</strong>
            </div>
            <div>
              <small style={{ color: '#667085', display: 'block' }}>20D Win Rate</small>
              <strong style={{ fontSize: '0.95rem', color: analysis.winRate >= 55 ? '#16a34a' : '#dc2626' }}>
                {(analysis.winRate || 0).toFixed(1)}%
              </strong>
            </div>
            <div>
              <small style={{ color: '#667085', display: 'block' }}>Median Return</small>
              <strong style={{ fontSize: '0.95rem', color: analysis.medianReturn > 0 ? '#16a34a' : '#dc2626' }}>
                {(analysis.medianReturn || 0).toFixed(2)}%
              </strong>
            </div>
            <div>
              <small style={{ color: '#667085', display: 'block' }}>Status</small>
              <strong style={{ fontSize: '0.95rem', color: isValidated ? '#16a34a' : '#dc2626' }}>
                {isValidated ? 'Validated' : 'Not Validated'}
              </strong>
            </div>
          </div>
        </div>
      )}

      {analysis?.similarSetups && analysis.similarSetups.length > 0 && (
        <div style={{ marginBottom: '10px' }}>
          <strong style={{ display: 'block', marginBottom: '6px', color: '#20242a' }}>Similar Historical Setups</strong>
          <div style={{ overflowX: 'auto' }}>
            <table style={{ width: '100%', fontSize: '0.75rem', borderCollapse: 'collapse' }}>
              <thead>
                <tr style={{ borderBottom: '1px solid #e4e7ec', background: '#f9fbfb' }}>
                  <th style={{ textAlign: 'left', padding: '4px 0', paddingRight: '8px', fontWeight: 600, color: '#667085' }}>Date</th>
                  <th style={{ textAlign: 'left', padding: '4px 0', paddingRight: '8px', fontWeight: 600, color: '#667085' }}>Outcome</th>
                  <th style={{ textAlign: 'right', padding: '4px 0', fontWeight: 600, color: '#667085' }}>20-Day Return</th>
                </tr>
              </thead>
              <tbody>
                {analysis.similarSetups.slice(0, 10).map((setup, idx) => {
                  const outcomeColor = setup.forwardReturn > 0 ? '#16a34a' : setup.forwardReturn < 0 ? '#dc2626' : '#9facbd';
                  const outcomeLabel = setup.forwardReturn > 0 ? 'Win' : setup.forwardReturn < 0 ? 'Loss' : 'Flat';
                  const setupDate = setup.date ? new Date(setup.date).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: '2-digit' }) : 'N/A';
                  return (
                    <tr key={idx} style={{ borderBottom: '1px solid #f0f1f3' }}>
                      <td style={{ padding: '4px 0', paddingRight: '8px', color: '#667085' }}>
                        {setupDate}
                      </td>
                      <td style={{ padding: '4px 0', paddingRight: '8px' }}>
                        <span style={{ color: outcomeColor, fontWeight: 600 }}>{outcomeLabel}</span>
                      </td>
                      <td style={{ padding: '4px 0', textAlign: 'right', color: outcomeColor, fontWeight: 600 }}>
                        {setup.forwardReturn > 0 ? '+' : ''}{setup.forwardReturn.toFixed(2)}%
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
          {analysis.similarSetups.length > 10 && (
            <small style={{ color: '#9facbd', marginTop: '4px', display: 'block' }}>
              ... and {analysis.similarSetups.length - 10} more setups
            </small>
          )}
        </div>
      )}

      {analysis?.invalidation && analysis.invalidation !== 'N/A' && (
        <div style={{ padding: '8px', background: '#fef2f2', borderLeft: '3px solid #fca5a5', borderRadius: '3px' }}>
          <small style={{ color: '#7f1d1d', display: 'block' }}>
            <strong>Invalidation:</strong> {analysis.invalidation}
          </small>
        </div>
      )}
    </div>
  );
}

function ResearchSurface({ selectedStrategy, symbolForm, setSymbolForm, addSymbol, strategyQuotes, strategyHistory, indicator }) {
  if (!selectedStrategy) return <Empty text="Create a strategy to start tracking symbols." />;
  return (
    <>
      <div className="strategyHeader">
        <div>
          <h3>{selectedStrategy.name}</h3>
          <p>{selectedStrategy.description}</p>
        </div>
        <span className="badge">{selectedStrategy.visibility}</span>
      </div>
      <form className="symbolForm" onSubmit={addSymbol}>
        <Field label="Symbol" value={symbolForm.symbol} onChange={(value) => setSymbolForm({ ...symbolForm, symbol: value })} required />
        <Field label="Note" value={symbolForm.note} onChange={(value) => setSymbolForm({ ...symbolForm, note: value })} />
        <button className="primary" type="submit"><Search size={17} />Track</button>
      </form>
      <div className="quoteGrid">
        {(strategyQuotes?.symbols || []).map((quote) => {
          const change = quote.change || 0;
          const changePercent = quote.percentChange || 0;
          const isPositive = change >= 0;
          const price = quote.lastPrice || quote.price || 0;
          const volume = quote.volume || 0;
          return (
            <article className="quote" key={quote.symbol}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline', marginBottom: '2px' }}>
                <strong style={{ fontSize: '1rem' }}>{quote.symbol}</strong>
                <span style={{ color: isPositive ? '#16a34a' : '#dc2626', fontSize: '0.85rem', fontWeight: 600 }}>
                  {isPositive ? '↑' : '↓'} {Math.abs(changePercent).toFixed(2)}%
                </span>
              </div>
              <span style={{ fontSize: '1.15rem', fontWeight: 700, color: '#20242a', marginBottom: '2px', display: 'block' }}>
                ${price.toFixed(2)}
              </span>
              <small style={{ color: '#667085', fontSize: '0.75rem' }}>
                Vol: {(volume / 1e6).toFixed(1)}M | Change: ${Math.abs(change).toFixed(2)}
              </small>
            </article>
          );
        })}
        {(!strategyQuotes?.symbols || strategyQuotes.symbols.length === 0) && <Empty text="Add a tracked symbol to activate this surface." />}
      </div>
      <div className="researchGrid">
        <div style={{ background: '#f9fbfb', border: '1px solid #e2e8f0', borderRadius: '7px', padding: '10px' }}>
          <h3 style={{ margin: '0 0 8px 0', fontSize: '0.95rem', fontWeight: 600, display: 'flex', gap: '8px', alignItems: 'center' }}>
            <BarChart3 size={16} />Price Data
          </h3>
          {strategyQuotes?.symbols && strategyQuotes.symbols.length > 0 ? (
            <div style={{ fontSize: '0.85rem' }}>
              <p style={{ margin: '2px 0' }}><strong>{strategyQuotes.symbols.length}</strong> symbols tracked</p>
              <p style={{ margin: '2px 0', color: '#667085' }}>Source: {strategyQuotes.dataFreshness || 'Real-time'}</p>
            </div>
          ) : (
            <p style={{ color: '#667085', fontSize: '0.85rem' }}>Add symbols to view data</p>
          )}
        </div>
        <div style={{ background: '#f9fbfb', border: '1px solid #e2e8f0', borderRadius: '7px', padding: '10px' }}>
          <h3 style={{ margin: '0 0 8px 0', fontSize: '0.95rem', fontWeight: 600, display: 'flex', gap: '8px', alignItems: 'center' }}>
            <ShieldCheck size={16} />Technical Indicators
          </h3>
          {strategyQuotes?.symbols && strategyQuotes.symbols.length > 0 ? (
            <IndicatorPanel
              symbol={strategyQuotes.symbols[0]?.symbol}
              indicator={indicator}
            />
          ) : (
            <p style={{ color: '#667085', fontSize: '0.85rem' }}>Add symbols to view indicators</p>
          )}
        </div>
      </div>
    </>
  );
}

function Metric({ icon, label, value, accent }) {
  return (
    <article className="metric">
      <div className="metricIcon">{React.cloneElement(icon, { size: 20 })}</div>
      <span>{label}</span>
      <strong>{value}</strong>
      {accent && <small>{accent}</small>}
    </article>
  );
}

function Panel({ title, icon, children, className = '', action, onAction }) {
  return (
    <section className={`panel ${className}`}>
      <div className="panelHeader">
        <h2>{React.cloneElement(icon, { size: 20 })}{title}</h2>
        {action && <button type="button" onClick={onAction}>{action}</button>}
      </div>
      {children}
    </section>
  );
}

function Field({ label, value, onChange, required, placeholder }) {
  return (
    <label>
      {label}
      <input value={value} required={required} placeholder={placeholder} onChange={(event) => onChange(event.target.value)} />
    </label>
  );
}

function TextField({ label, value, onChange, required, placeholder }) {
  return (
    <label>
      {label}
      <textarea value={value} required={required} placeholder={placeholder} onChange={(event) => onChange(event.target.value)} />
    </label>
  );
}

function Score({ label, value }) {
  return (
    <div className="score">
      <span>{label}</span>
      <strong>{value}</strong>
    </div>
  );
}

function Badge({ label, value }) {
  return (
    <div className="badgeBox">
      <span>{label}</span>
      <strong>{value}</strong>
    </div>
  );
}

function DecisionJournalTimeline({ decisions, onCloseDecision, onCardClick, showEmpty = true }) {
  const [hoveredCardId, setHoveredCardId] = React.useState(null);
  const [flashingCards, setFlashingCards] = React.useState(new Set());
  const [liveQuotes, setLiveQuotes] = React.useState({});

  // Fetch live quotes for all open positions
  React.useEffect(() => {
    const symbols = decisions
      .filter(d => d.status === 'open' && (d.symbol || d.ticker))
      .map(d => d.symbol || d.ticker);

    if (symbols.length === 0) return;

    const fetchQuotes = async () => {
      try {
        const response = await fetch(`/finance-api/quote?symbols=${symbols.join(',')}`);
        if (response.ok) {
          const data = await response.json();
          setLiveQuotes(data.quotes || {});
        }
      } catch (error) {
        console.log('Live quote fetch failed (optional feature):', error);
      }
    };

    fetchQuotes();
    const interval = setInterval(fetchQuotes, 5000); // Update every 5 seconds
    return () => clearInterval(interval);
  }, [decisions]);

  // Calculate P/L for a decision
  const calculatePnL = (decision) => {
    if (decision.status !== 'open' || !decision.price || !decision.quantity) {
      return null;
    }

    const symbol = decision.symbol || decision.ticker;
    const quote = liveQuotes[symbol];
    if (!quote?.lastPrice) return null;

    const entryValue = decision.price * decision.quantity;
    const currentValue = quote.lastPrice * decision.quantity;
    const pnl = currentValue - entryValue;
    const pnlPct = (pnl / entryValue) * 100;

    return { pnl, pnlPct, currentPrice: quote.lastPrice };
  };

  // Track P/L changes for flash effect
  const prevPnLRef = React.useRef({});
  React.useEffect(() => {
    decisions.forEach(d => {
      if (d.status !== 'open') return;

      const current = calculatePnL(d);
      const prev = prevPnLRef.current[d.id];

      if (prev && current && Math.abs(current.pnl - prev.pnl) > 50) {
        setFlashingCards(prev => new Set([...prev, d.id]));
        setTimeout(() => {
          setFlashingCards(prev => {
            const next = new Set(prev);
            next.delete(d.id);
            return next;
          });
        }, 800);
      }

      prevPnLRef.current[d.id] = current;
    });
  }, [liveQuotes, decisions]);

  // Group decisions by date (newest first)
  const decisionsByDate = decisions.reduce((acc, decision) => {
    const dateStr = decision.createdAt ? new Date(decision.createdAt).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' }) : 'Unknown';
    if (!acc[dateStr]) acc[dateStr] = [];
    acc[dateStr].push(decision);
    return acc;
  }, {});

  const sortedDates = Object.keys(decisionsByDate).sort((a, b) => new Date(b) - new Date(a));

  const formatEditTime = (timestamp) => {
    if (!timestamp) return '';
    const date = new Date(timestamp);
    return date.toLocaleString('en-US', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' });
  };

  const getActionColor = (type) => {
    switch (type) {
      case 'BUY': return '#16a34a';
      case 'SELL': return '#dc2626';
      case 'buy': return '#16a34a';
      case 'sell': return '#dc2626';
      case 'watch': return '#0f766e';
      case 'avoid': return '#ea580c';
      default: return '#9facbd';
    }
  };

  const getStatusColor = (status) => {
    switch (status) {
      case 'open': return '#16a34a';
      case 'active': return '#16a34a';
      case 'closed': return '#9facbd';
      case 'archived': return '#d1d5db';
      default: return '#9facbd';
    }
  };

  return (
    <div style={{ position: 'relative' }}>
      {sortedDates.length > 0 && (
        <>
          {/* Timeline line */}
          <div style={{
            position: 'absolute',
            left: '19px',
            top: '30px',
            bottom: 0,
            width: '1px',
            backgroundColor: '#e4e7ec'
          }} />

          {/* Decision cards by date */}
          {sortedDates.map((dateStr) => (
            <div key={dateStr} style={{ marginBottom: '24px' }}>
              {/* Date header with timeline dot */}
              <div style={{ display: 'flex', alignItems: 'center', marginBottom: '12px', position: 'relative', zIndex: 1 }}>
                <div style={{
                  width: '40px',
                  height: '40px',
                  borderRadius: '50%',
                  background: '#ffffff',
                  border: '3px solid #13a79a',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  fontSize: '18px',
                  marginRight: '12px'
                }}>
                  📅
                </div>
                <span style={{ fontSize: '0.95rem', fontWeight: 600, color: '#526071' }}>{dateStr}</span>
              </div>

              {/* Decision cards for this date */}
              <div style={{ marginLeft: '40px', display: 'flex', flexDirection: 'column', gap: '12px' }}>
                {decisionsByDate[dateStr].map((decision) => (
                  <div
                    key={decision.id}
                    style={{
                      background: '#ffffff',
                      border: '1px solid #e4e7ec',
                      borderRadius: '8px',
                      padding: '16px',
                      transition: 'all 0.2s ease',
                      cursor: 'pointer',
                      position: 'relative'
                    }}
                    className="decision-card"
                    onClick={() => onCardClick && onCardClick(decision)}
                    onMouseEnter={(e) => {
                      e.currentTarget.style.boxShadow = '0 4px 12px rgba(0,0,0,0.08)';
                      e.currentTarget.style.borderColor = '#d7dce2';
                      setHoveredCardId(decision.id);
                    }}
                    onMouseLeave={(e) => {
                      e.currentTarget.style.boxShadow = 'none';
                      e.currentTarget.style.borderColor = '#e4e7ec';
                      setHoveredCardId(null);
                    }}
                  >
                    {/* Edit History Tooltip */}
                    {hoveredCardId === decision.id && decision.editHistory && decision.editHistory.length > 0 && (
                      <div style={{
                        position: 'absolute',
                        top: '-12px',
                        right: '16px',
                        background: '#1f2937',
                        color: '#ffffff',
                        padding: '10px 12px',
                        borderRadius: '6px',
                        fontSize: '0.75rem',
                        zIndex: 100,
                        boxShadow: '0 4px 12px rgba(0,0,0,0.15)',
                        minWidth: '200px',
                        maxWidth: '300px',
                        lineHeight: '1.4'
                      }}>
                        <div style={{ fontWeight: 600, marginBottom: '6px', borderBottom: '1px solid #4b5563', paddingBottom: '6px' }}>
                          Edit History
                        </div>
                        {decision.editHistory.slice(0, 5).map((edit, idx) => (
                          <div key={idx} style={{ marginBottom: '4px', fontSize: '0.7rem', opacity: 0.9 }}>
                            <div style={{ color: '#9ca3af', fontSize: '0.65rem' }}>
                              {formatEditTime(edit.editedAt)}
                            </div>
                            <div>
                              <strong>{edit.field}</strong>: {edit.newValue?.substring(0, 50)}
                              {edit.newValue?.length > 50 ? '...' : ''}
                            </div>
                          </div>
                        ))}
                        {decision.editHistory.length > 5 && (
                          <div style={{ marginTop: '6px', fontSize: '0.7rem', color: '#9ca3af', fontStyle: 'italic' }}>
                            +{decision.editHistory.length - 5} more edits
                          </div>
                        )}
                      </div>
                    )}

                    {/* Card Header: Action + Ticker */}
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'start', marginBottom: '8px' }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                        <span style={{
                          display: 'inline-block',
                          padding: '6px 12px',
                          borderRadius: '6px',
                          background: getActionColor(decision.action || decision.decisionType) + '20',
                          color: getActionColor(decision.action || decision.decisionType),
                          fontSize: '0.75rem',
                          fontWeight: 700,
                          textTransform: 'uppercase',
                          letterSpacing: '0.5px'
                        }}>
                          {decision.action ? decision.action : decision.decisionType}
                        </span>
                        <strong style={{ fontSize: '1rem', color: '#20242a' }}>
                          {decision.symbol || decision.ticker}
                        </strong>
                      </div>
                      <span style={{
                        fontSize: '0.7rem',
                        fontWeight: 600,
                        color: getStatusColor(decision.status),
                        textTransform: 'uppercase',
                        background: getStatusColor(decision.status) + '15',
                        padding: '4px 8px',
                        borderRadius: '4px'
                      }}>
                        {decision.status}
                      </span>
                    </div>

                    {/* Card Body: Quantity and Price */}
                    {decision.quantity !== undefined && decision.price !== undefined && (
                      <div style={{ fontSize: '0.9rem', color: '#667085', marginBottom: '8px' }}>
                        {decision.quantity} shares @ ${typeof decision.price === 'number' ? decision.price.toFixed(2) : decision.price}
                      </div>
                    )}

                    {/* Thesis / Title */}
                    {(decision.thesis || decision.title) && (
                      <div style={{ fontSize: '0.85rem', color: '#526071', marginBottom: '8px', fontStyle: 'italic' }}>
                        {decision.thesis || decision.title}
                      </div>
                    )}

                    {/* P/L Display for Open Positions */}
                    {decision.status === 'open' && (() => {
                      const liveData = calculatePnL(decision);
                      const displayPnl = liveData || decision.currentPnl;
                      const isFlashing = flashingCards.has(decision.id);

                      if (!displayPnl) return null;

                      const pnlValue = liveData?.pnl ?? displayPnl;
                      const pnlPct = liveData?.pnlPct ?? (typeof displayPnl === 'object' ? displayPnl.pnlPct : 0);

                      return (
                        <div style={{
                          background: pnlValue >= 0 ? '#f0fdf4' : '#fef2f2',
                          border: `1px solid ${pnlValue >= 0 ? '#dcfce7' : '#fee2e2'}`,
                          borderRadius: '6px',
                          padding: '8px',
                          marginTop: '8px',
                          display: 'flex',
                          justifyContent: 'space-between',
                          alignItems: 'center',
                          transition: 'all 0.3s ease',
                          animation: isFlashing ? 'pnl-flash 0.8s ease-out' : 'none',
                          boxShadow: isFlashing ? `0 0 12px ${pnlValue >= 0 ? '#16a34a' : '#dc2626'}40` : 'none'
                        }}>
                          <span style={{ fontSize: '0.75rem', color: '#667085' }}>
                            Current P/L {liveData && '(Live)'}
                          </span>
                          <div style={{
                            fontSize: '0.9rem',
                            fontWeight: 700,
                            color: pnlValue >= 0 ? '#16a34a' : '#dc2626'
                          }}>
                            {pnlValue >= 0 ? '+' : ''}{Number(pnlValue).toFixed(2)} ({pnlPct >= 0 ? '+' : ''}{Number(pnlPct).toFixed(1)}%)
                          </div>
                        </div>
                      );
                    })()}

                    {/* Alert Indicators */}
                    {decision.alerts && decision.alerts.length > 0 && decision.status === 'open' && (
                      <div style={{ marginTop: '10px', paddingTop: '10px', borderTop: '1px solid #e4e7ec' }}>
                        <span style={{ fontSize: '0.7rem', color: '#9facbd', textTransform: 'uppercase', fontWeight: 600 }}>Exit Criteria</span>
                        <div style={{ display: 'flex', flexDirection: 'column', gap: '6px', marginTop: '6px' }}>
                          {decision.alerts.map((alert, idx) => (
                            <div
                              key={idx}
                              style={{
                                fontSize: '0.75rem',
                                color: alert.triggered ? '#dc2626' : '#667085',
                                display: 'flex',
                                justifyContent: 'space-between',
                                alignItems: 'center',
                                padding: '6px 8px',
                                background: alert.triggered ? '#fef2f2' : '#f9fbfb',
                                borderRadius: '4px',
                                border: `1px solid ${alert.triggered ? '#fee2e2' : '#e4e7ec'}`
                              }}
                            >
                              <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                                <span>{alert.type === 'price_above' ? '↑' : '↓'}</span>
                                <span>${alert.value}</span>
                                {alert.triggered && <span style={{ fontWeight: 600 }}>⚠️ Hit</span>}
                              </div>
                            </div>
                          ))}
                        </div>

                        {/* Alert Action Buttons (visible when alert triggered) */}
                        {decision.alerts.some(a => a.triggered) && onCloseDecision && (
                          <div style={{ display: 'flex', gap: '6px', marginTop: '8px' }}>
                            <button
                              onClick={(e) => {
                                e.stopPropagation();
                                onCloseDecision(decision.id, decision);
                              }}
                              style={{
                                flex: 1,
                                padding: '6px 10px',
                                background: '#dc2626',
                                color: '#ffffff',
                                border: 'none',
                                borderRadius: '4px',
                                fontSize: '0.75rem',
                                fontWeight: 600,
                                cursor: 'pointer',
                                transition: 'all 0.2s ease'
                              }}
                              onMouseEnter={(e) => e.target.style.background = '#b91c1c'}
                              onMouseLeave={(e) => e.target.style.background = '#dc2626'}
                            >
                              ✓ Close Decision
                            </button>
                            <button
                              onClick={(e) => {
                                e.stopPropagation();
                                // Leave open - just dismiss the alert indicator
                              }}
                              style={{
                                flex: 1,
                                padding: '6px 10px',
                                background: '#f3f4f6',
                                color: '#667085',
                                border: '1px solid #e5e7eb',
                                borderRadius: '4px',
                                fontSize: '0.75rem',
                                fontWeight: 600,
                                cursor: 'pointer',
                                transition: 'all 0.2s ease'
                              }}
                              onMouseEnter={(e) => {
                                e.target.style.background = '#e5e7eb';
                                e.target.style.borderColor = '#d1d5db';
                              }}
                              onMouseLeave={(e) => {
                                e.target.style.background = '#f3f4f6';
                                e.target.style.borderColor = '#e5e7eb';
                              }}
                            >
                              ← Leave Open
                            </button>
                          </div>
                        )}
                      </div>
                    )}
                  </div>
                ))}
              </div>
            </div>
          ))}
        </>
      )}
      {showEmpty && decisions.length === 0 && <Empty text="No decisions to display." />}
    </div>
  );
}

function AlertTriggeredModal({ alert, decision, onClose, onCloseDecision }) {
  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()} style={{ maxWidth: '450px' }}>
        <div className="modal-header">
          <h2 style={{ color: '#dc2626' }}>🎯 Exit Alert Triggered</h2>
          <button className="modal-close" onClick={onClose}>✕</button>
        </div>

        <div className="modal-body">
          <div style={{ marginBottom: '16px', padding: '12px', background: '#fef2f2', borderRadius: '6px', border: '1px solid #fee2e2' }}>
            <div style={{ fontSize: '0.85rem', color: '#991b1b', marginBottom: '8px' }}>
              Your exit alert for <strong>{decision.symbol || decision.ticker}</strong> has been triggered!
            </div>
            <div style={{ fontSize: '0.9rem', fontWeight: 600, color: '#20242a' }}>
              {alert.description || `${alert.conditionType} $${alert.conditionValue}`}
            </div>
            {alert.triggeredPrice && (
              <div style={{ fontSize: '0.8rem', color: '#667085', marginTop: '6px' }}>
                Triggered at: <strong>${alert.triggeredPrice.toFixed(2)}</strong>
              </div>
            )}
          </div>

          <div style={{ marginBottom: '20px', padding: '12px', background: '#f9fbfb', borderRadius: '6px', border: '1px solid #e4e7ec' }}>
            <div style={{ fontSize: '0.75rem', fontWeight: 600, color: '#667085', textTransform: 'uppercase', marginBottom: '8px' }}>Current Position</div>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
              <div>
                <div style={{ fontSize: '0.7rem', color: '#9facbd' }}>Entry</div>
                <div style={{ fontSize: '0.9rem', fontWeight: 600, color: '#20242a' }}>
                  ${decision.price?.toFixed(2) || '—'} × {decision.quantity}
                </div>
              </div>
              <div>
                <div style={{ fontSize: '0.7rem', color: '#9facbd' }}>Entry Value</div>
                <div style={{ fontSize: '0.9rem', fontWeight: 600, color: '#20242a' }}>
                  ${(decision.price * decision.quantity).toFixed(2)}
                </div>
              </div>
            </div>
          </div>

          <div style={{ marginBottom: '16px' }}>
            <div style={{ fontSize: '0.85rem', fontWeight: 600, color: '#667085', marginBottom: '8px' }}>What would you like to do?</div>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
              <button
                onClick={() => onCloseDecision(decision, alert)}
                style={{
                  padding: '12px 16px',
                  background: '#dc2626',
                  color: '#ffffff',
                  border: 'none',
                  borderRadius: '6px',
                  fontSize: '0.9rem',
                  fontWeight: 600,
                  cursor: 'pointer',
                  transition: 'background 0.2s'
                }}
                onMouseEnter={(e) => { e.target.style.background = '#b91c1c'; }}
                onMouseLeave={(e) => { e.target.style.background = '#dc2626'; }}
              >
                ✓ Close Decision
              </button>
              <button
                onClick={onClose}
                style={{
                  padding: '12px 16px',
                  background: '#f9fbfb',
                  color: '#667085',
                  border: '1px solid #d7dce2',
                  borderRadius: '6px',
                  fontSize: '0.9rem',
                  fontWeight: 600,
                  cursor: 'pointer',
                  transition: 'all 0.2s'
                }}
                onMouseEnter={(e) => {
                  e.target.style.background = '#f3f4f6';
                  e.target.style.borderColor = '#d1d5db';
                }}
                onMouseLeave={(e) => {
                  e.target.style.background = '#f9fbfb';
                  e.target.style.borderColor = '#d7dce2';
                }}
              >
                ← Leave Open
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

function ReviewScheduler({ decision }) {
  const calculateReviewDates = (transactionDate) => {
    const date = new Date(transactionDate);
    const reviews = [
      { days: 30, label: '30-Day Review' },
      { days: 90, label: '90-Day Review' },
      { days: 180, label: '180-Day Review' },
      { days: 365, label: '1-Year Review' }
    ];

    return reviews.map(review => {
      const reviewDate = new Date(date);
      reviewDate.setDate(reviewDate.getDate() + review.days);
      const today = new Date();
      const isPast = reviewDate < today;
      const daysUntil = Math.ceil((reviewDate - today) / (1000 * 60 * 60 * 24));

      return {
        ...review,
        reviewDate: reviewDate.toLocaleDateString(),
        isPast,
        daysUntil: isPast ? 0 : daysUntil,
        status: isPast ? 'overdue' : 'pending'
      };
    });
  };

  const reviewDates = calculateReviewDates(decision.transactionDate);

  return (
    <div style={{ marginBottom: '16px', paddingBottom: '16px', borderBottom: '1px solid #e4e7ec' }}>
      <h3 style={{ fontSize: '0.9rem', fontWeight: 600, color: '#667085', margin: '0 0 12px 0' }}>Review Schedule</h3>
      <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
        {reviewDates.map((review, idx) => (
          <div key={idx} style={{ display: 'flex', alignItems: 'center', padding: '10px', background: '#f9fbfb', borderRadius: '4px', border: `1px solid ${review.isPast ? '#fee2e2' : '#bfdbfe'}` }}>
            <div style={{ flex: 1 }}>
              <div style={{ fontSize: '0.85rem', fontWeight: 600, color: '#20242a' }}>{review.label}</div>
              <div style={{ fontSize: '0.75rem', color: '#9facbd' }}>
                {review.reviewDate} {!review.isPast && `(in ${review.daysUntil} days)`}
              </div>
            </div>
            <div style={{ fontSize: '0.75rem', fontWeight: 600, color: review.isPast ? '#dc2626' : '#0ea5e9', padding: '4px 8px', background: review.isPast ? '#fef2f2' : '#f0f9ff', borderRadius: '3px' }}>
              {review.isPast ? 'Overdue' : 'Pending'}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

function DecisionDetailModal({ decision, onClose, editForm, setEditForm, api, onSaveSuccess }) {
  const [isEditing, setIsEditing] = React.useState(decision.status === 'open');
  const [isClosing, setIsClosing] = React.useState(false);
  const [closeReason, setCloseReason] = React.useState('');
  const [exitPrice, setExitPrice] = React.useState('');
  const [isSaving, setIsSaving] = React.useState(false);
  const [alerts, setAlerts] = React.useState(decision.alerts || []);
  const [isAddingAlert, setIsAddingAlert] = React.useState(false);
  const [newAlert, setNewAlert] = React.useState({ conditionType: 'PRICE_ABOVE', conditionValue: '', description: '' });

  if (!decision) return null;

  const thesisSuggestions = [
    'Stock is undervalued',
    'Technical breakout signal',
    'Momentum play',
    'Mean reversion (RSI < 30)',
    'Matches my strategy'
  ];

  const evidenceSuggestions = [
    'P/E below sector average',
    'RSI shows oversold (< 30)',
    'Price above 50-day MA',
    'Recent earnings beat',
    'Sector showing strength'
  ];

  const risksSuggestions = [
    'Market downturn or correction',
    'Earnings miss',
    'Sector rotation',
    'Valuation compression',
    'Geopolitical risk'
  ];

  const handleSaveEdit = async () => {
    try {
      setIsSaving(true);
      const payload = {
        thesis: editForm.thesis || '',
        evidence: editForm.evidence || '',
        risks: editForm.risks || '',
        comments: editForm.comments || ''
      };

      await api(`/api/decisions/${decision.id}`, {
        method: 'PUT',
        body: JSON.stringify(payload)
      });

      setIsEditing(false);
      if (onSaveSuccess) onSaveSuccess();
    } catch (error) {
      alert(`Failed to save decision: ${error.message}`);
    } finally {
      setIsSaving(false);
    }
  };

  const handleCloseDecision = async () => {
    if (!exitPrice || parseFloat(exitPrice) <= 0) {
      alert('Please enter a valid exit price');
      return;
    }

    try {
      setIsSaving(true);
      const exitPriceValue = parseFloat(exitPrice);
      const entryValue = decision.price * decision.quantity;
      const exitValue = exitPriceValue * decision.quantity;
      const finalPnL = exitValue - entryValue;
      const finalPnLPct = (finalPnL / entryValue) * 100;

      const closePayload = {
        exit_price: exitPriceValue,
        exit_pnl: finalPnL,
        close_reason: closeReason || ''
      };

      await api(`/api/decisions/${decision.id}/close`, {
        method: 'POST',
        body: JSON.stringify(closePayload)
      });

      alert(`Decision closed at $${exitPriceValue.toFixed(2)}\nFinal P/L: ${finalPnL >= 0 ? '+' : ''}$${finalPnL.toFixed(2)} (${finalPnLPct >= 0 ? '+' : ''}${finalPnLPct.toFixed(1)}%)`);
      if (onSaveSuccess) onSaveSuccess();
      onClose();
    } catch (error) {
      alert(`Failed to close decision: ${error.message}`);
    } finally {
      setIsSaving(false);
    }
  };

  const handleAddAlert = async () => {
    if (!newAlert.conditionValue || parseFloat(newAlert.conditionValue) <= 0) {
      alert('Please enter a valid condition value');
      return;
    }

    try {
      const payload = {
        condition_type: newAlert.conditionType,
        condition_value: parseFloat(newAlert.conditionValue),
        description: newAlert.description
      };

      const response = await api(`/api/decisions/${decision.id}/exit-criteria`, {
        method: 'POST',
        body: JSON.stringify(payload)
      });

      setAlerts([...alerts, response]);
      setNewAlert({ conditionType: 'PRICE_ABOVE', conditionValue: '', description: '' });
      setIsAddingAlert(false);
      if (onSaveSuccess) onSaveSuccess();
    } catch (error) {
      alert(`Failed to add alert: ${error.message}`);
    }
  };

  const handleDeleteAlert = async (alertId) => {
    try {
      await api(`/api/decisions/${decision.id}/exit-criteria/${alertId}`, {
        method: 'DELETE'
      });

      setAlerts(alerts.filter(a => a.id !== alertId));
      if (onSaveSuccess) onSaveSuccess();
    } catch (error) {
      alert(`Failed to delete alert: ${error.message}`);
    }
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()} style={{ maxWidth: '700px', maxHeight: '90vh', overflowY: 'auto' }}>
        <div className="modal-header">
          <h2>Decision Details</h2>
          <button className="modal-close" onClick={onClose}>✕</button>
        </div>

        <div className="modal-body">
          {/* Read-only Transaction Details */}
          <div style={{ background: '#f9fbfb', padding: '12px', borderRadius: '6px', marginBottom: '16px', border: '1px solid #e4e7ec' }}>
            <div style={{ fontSize: '0.75rem', color: '#9facbd', fontWeight: 600, textTransform: 'uppercase', marginBottom: '8px' }}>
              Transaction (Locked)
            </div>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '12px' }}>
              <div>
                <div style={{ fontSize: '0.7rem', color: '#667085', marginBottom: '4px' }}>Symbol</div>
                <div style={{ fontSize: '0.9rem', fontWeight: 600, color: '#20242a' }}>{decision.symbol || decision.ticker}</div>
              </div>
              <div>
                <div style={{ fontSize: '0.7rem', color: '#667085', marginBottom: '4px' }}>Action</div>
                <div style={{ fontSize: '0.9rem', fontWeight: 600, color: '#20242a' }}>{decision.action}</div>
              </div>
              <div>
                <div style={{ fontSize: '0.7rem', color: '#667085', marginBottom: '4px' }}>Quantity</div>
                <div style={{ fontSize: '0.9rem', fontWeight: 600, color: '#20242a' }}>{decision.quantity} shares</div>
              </div>
              <div>
                <div style={{ fontSize: '0.7rem', color: '#667085', marginBottom: '4px' }}>Entry Price</div>
                <div style={{ fontSize: '0.9rem', fontWeight: 600, color: '#20242a' }}>${decision.price?.toFixed(2)}</div>
              </div>
            </div>
          </div>

          {/* Status & P/L Display */}
          {decision.status === 'open' && (
            <div style={{ background: '#f0fdf4', padding: '12px', borderRadius: '6px', marginBottom: '16px', border: '1px solid #dcfce7' }}>
              <div style={{ fontSize: '0.75rem', color: '#667085', fontWeight: 600, marginBottom: '8px' }}>Current Position</div>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <div>
                  <div style={{ fontSize: '0.8rem', color: '#667085' }}>Entry Value</div>
                  <div style={{ fontSize: '0.95rem', fontWeight: 600, color: '#20242a' }}>
                    ${(decision.price * decision.quantity).toFixed(2)}
                  </div>
                </div>
                {decision.currentPnl !== undefined && (
                  <div>
                    <div style={{ fontSize: '0.8rem', color: '#667085' }}>Current P/L</div>
                    <div style={{ fontSize: '0.95rem', fontWeight: 600, color: decision.currentPnl >= 0 ? '#16a34a' : '#dc2626' }}>
                      {decision.currentPnl >= 0 ? '+' : ''}{decision.currentPnl.toFixed(2)}
                    </div>
                  </div>
                )}
              </div>
            </div>
          )}

          {/* Closed Decision Summary */}
          {decision.status === 'closed' && (
            <div style={{
              background: '#f9fbfb',
              padding: '14px',
              borderRadius: '6px',
              marginBottom: '16px',
              border: '1px solid #e4e7ec',
              opacity: 0.9
            }}>
              <div style={{ fontSize: '0.75rem', color: '#667085', fontWeight: 600, marginBottom: '10px', textTransform: 'uppercase' }}>
                Decision Closed
              </div>
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '12px', marginBottom: '10px' }}>
                <div>
                  <div style={{ fontSize: '0.7rem', color: '#9facbd', marginBottom: '4px' }}>Entry Value</div>
                  <div style={{ fontSize: '0.9rem', fontWeight: 600, color: '#20242a' }}>
                    ${(decision.price * decision.quantity).toFixed(2)}
                  </div>
                </div>
                <div>
                  <div style={{ fontSize: '0.7rem', color: '#9facbd', marginBottom: '4px' }}>Exit Value</div>
                  <div style={{ fontSize: '0.9rem', fontWeight: 600, color: '#20242a' }}>
                    ${decision.exitValue?.toFixed(2) || '—'}
                  </div>
                </div>
                <div style={{
                  padding: '8px',
                  background: decision.finalPnL >= 0 ? '#f0fdf4' : '#fef2f2',
                  borderRadius: '4px',
                  border: `1px solid ${decision.finalPnL >= 0 ? '#dcfce7' : '#fee2e2'}`
                }}>
                  <div style={{ fontSize: '0.7rem', color: '#667085', marginBottom: '4px' }}>Final P/L</div>
                  <div style={{ fontSize: '0.9rem', fontWeight: 600, color: decision.finalPnL >= 0 ? '#16a34a' : '#dc2626' }}>
                    {decision.finalPnL >= 0 ? '+' : ''}{decision.finalPnL?.toFixed(2) || '—'}
                  </div>
                  <div style={{ fontSize: '0.7rem', color: decision.finalPnL >= 0 ? '#16a34a' : '#dc2626' }}>
                    {decision.finalPnLPct >= 0 ? '+' : ''}{decision.finalPnLPct?.toFixed(1) || '0.0'}%
                  </div>
                </div>
              </div>
              {decision.closeReason && (
                <div style={{ fontSize: '0.75rem', color: '#667085', paddingTop: '8px', borderTop: '1px solid #e4e7ec' }}>
                  <span style={{ fontWeight: 600 }}>Closed:</span> {decision.closeReason}
                  {decision.closedAt && (
                    <span style={{ color: '#9facbd', marginLeft: '8px' }}>
                      ({new Date(decision.closedAt).toLocaleDateString()})
                    </span>
                  )}
                </div>
              )}
            </div>
          )}

          {/* Review Schedule */}
          {decision.status === 'open' && <ReviewScheduler decision={decision} />}

          {/* Edit Form (for open decisions) */}
          {isEditing && decision.status === 'open' && (
            <form onSubmit={(e) => { e.preventDefault(); handleSaveEdit(); }}>
              <div className="form-section">
                <h3>Thesis</h3>
                <textarea
                  placeholder="Why did you make this trade?"
                  value={editForm.thesis || ''}
                  onChange={(e) => setEditForm({ ...editForm, thesis: e.target.value })}
                  className="form-textarea"
                  rows="2"
                />
              </div>

              <div className="form-section">
                <h3>Evidence</h3>
                <textarea
                  placeholder="What data or signals support this decision?"
                  value={editForm.evidence || ''}
                  onChange={(e) => setEditForm({ ...editForm, evidence: e.target.value })}
                  className="form-textarea"
                  rows="2"
                />
              </div>

              <div className="form-section">
                <h3>Risks</h3>
                <textarea
                  placeholder="What could go wrong?"
                  value={editForm.risks || ''}
                  onChange={(e) => setEditForm({ ...editForm, risks: e.target.value })}
                  className="form-textarea"
                  rows="2"
                />
              </div>

              <div className="form-section">
                <h3>Comments</h3>
                <textarea
                  placeholder="Additional notes..."
                  value={editForm.comments || ''}
                  onChange={(e) => setEditForm({ ...editForm, comments: e.target.value })}
                  className="form-textarea"
                  rows="2"
                />
              </div>

              <div style={{ display: 'flex', gap: '8px', marginBottom: '16px' }}>
                <button type="submit" className="btn-primary" style={{ flex: 1 }} disabled={isSaving}>
                  {isSaving ? 'Saving...' : 'Save Changes'}
                </button>
                <button type="button" onClick={() => setIsEditing(false)} className="btn-secondary" style={{ flex: 1 }} disabled={isSaving}>Cancel</button>
              </div>
            </form>
          )}

          {/* Read-only View (for closed decisions) */}
          {!isEditing && (
            <>
              {editForm.thesis && (
                <div style={{ marginBottom: '12px' }}>
                  <h3 style={{ fontSize: '0.85rem', fontWeight: 600, color: '#667085', margin: '0 0 4px 0' }}>Thesis</h3>
                  <div style={{ fontSize: '0.85rem', color: '#526071', padding: '8px', background: '#f9fbfb', borderRadius: '4px' }}>
                    {editForm.thesis}
                  </div>
                </div>
              )}
              {editForm.evidence && (
                <div style={{ marginBottom: '12px' }}>
                  <h3 style={{ fontSize: '0.85rem', fontWeight: 600, color: '#667085', margin: '0 0 4px 0' }}>Evidence</h3>
                  <div style={{ fontSize: '0.85rem', color: '#526071', padding: '8px', background: '#f9fbfb', borderRadius: '4px' }}>
                    {editForm.evidence}
                  </div>
                </div>
              )}
              {editForm.risks && (
                <div style={{ marginBottom: '12px' }}>
                  <h3 style={{ fontSize: '0.85rem', fontWeight: 600, color: '#667085', margin: '0 0 4px 0' }}>Risks</h3>
                  <div style={{ fontSize: '0.85rem', color: '#526071', padding: '8px', background: '#f9fbfb', borderRadius: '4px' }}>
                    {editForm.risks}
                  </div>
                </div>
              )}
              {editForm.comments && (
                <div style={{ marginBottom: '12px' }}>
                  <h3 style={{ fontSize: '0.85rem', fontWeight: 600, color: '#667085', margin: '0 0 4px 0' }}>Comments</h3>
                  <div style={{ fontSize: '0.85rem', color: '#526071', padding: '8px', background: '#f9fbfb', borderRadius: '4px' }}>
                    {editForm.comments}
                  </div>
                </div>
              )}
            </>
          )}

          {/* Exit Criteria Management */}
          {decision.status === 'open' && !isEditing && (
            <div style={{ marginBottom: '16px', paddingBottom: '16px', borderBottom: '1px solid #e4e7ec' }}>
              <h3 style={{ fontSize: '0.9rem', fontWeight: 600, color: '#667085', margin: '0 0 12px 0' }}>Exit Criteria & Alerts</h3>

              {/* Existing Alerts */}
              {alerts.length > 0 && (
                <div style={{ marginBottom: '12px' }}>
                  {alerts.map(alert => (
                    <div key={alert.id} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '10px', background: '#f9fbfb', borderRadius: '4px', marginBottom: '8px', border: `1px solid ${alert.status === 'TRIGGERED' ? '#fca5a5' : '#e4e7ec'}` }}>
                      <div style={{ flex: 1 }}>
                        <div style={{ fontSize: '0.85rem', fontWeight: 600, color: '#20242a' }}>
                          {alert.description || `${alert.conditionType} $${alert.conditionValue}`}
                        </div>
                        <div style={{ fontSize: '0.75rem', color: '#9facbd' }}>
                          {alert.status === 'TRIGGERED' && <span style={{ color: '#dc2626' }}>🎯 TRIGGERED</span>}
                          {alert.status === 'PENDING' && <span style={{ color: '#667085' }}>Pending</span>}
                          {alert.status === 'CLOSED' && <span style={{ color: '#9facbd' }}>Closed</span>}
                        </div>
                      </div>
                      {alert.status !== 'TRIGGERED' && alert.status !== 'CLOSED' && (
                        <button onClick={() => handleDeleteAlert(alert.id)} style={{ padding: '4px 8px', fontSize: '0.75rem', color: '#dc2626', background: 'transparent', border: '1px solid #fee2e2', borderRadius: '3px', cursor: 'pointer' }}>
                          Remove
                        </button>
                      )}
                    </div>
                  ))}
                </div>
              )}

              {/* Add New Alert Form */}
              {!isAddingAlert ? (
                <button onClick={() => setIsAddingAlert(true)} style={{ padding: '8px 12px', fontSize: '0.85rem', color: '#0ea5e9', background: '#f0f9ff', border: '1px solid #bfdbfe', borderRadius: '4px', cursor: 'pointer', fontWeight: 500 }}>
                  + Add Exit Criteria
                </button>
              ) : (
                <div style={{ padding: '12px', background: '#f0f9ff', borderRadius: '4px', border: '1px solid #bfdbfe' }}>
                  <div style={{ marginBottom: '10px' }}>
                    <label style={{ fontSize: '0.75rem', fontWeight: 600, color: '#0c4a6e', display: 'block', marginBottom: '4px' }}>Condition Type</label>
                    <select value={newAlert.conditionType} onChange={(e) => setNewAlert({ ...newAlert, conditionType: e.target.value })} style={{ width: '100%', padding: '6px', fontSize: '0.85rem', border: '1px solid #bfdbfe', borderRadius: '3px' }}>
                      <option value="PRICE_ABOVE">Price ≥ (take profit)</option>
                      <option value="PRICE_BELOW">Price ≤ (stop loss)</option>
                      <option value="PRICE_AT">Price = (exact target)</option>
                    </select>
                  </div>

                  <div style={{ marginBottom: '10px' }}>
                    <label style={{ fontSize: '0.75rem', fontWeight: 600, color: '#0c4a6e', display: 'block', marginBottom: '4px' }}>Condition Value</label>
                    <input type="number" step="0.01" min="0" value={newAlert.conditionValue} onChange={(e) => setNewAlert({ ...newAlert, conditionValue: e.target.value })} placeholder="Target price" style={{ width: '100%', padding: '6px', fontSize: '0.85rem', border: '1px solid #bfdbfe', borderRadius: '3px', boxSizing: 'border-box' }} />
                  </div>

                  <div style={{ marginBottom: '12px' }}>
                    <label style={{ fontSize: '0.75rem', fontWeight: 600, color: '#0c4a6e', display: 'block', marginBottom: '4px' }}>Description (optional)</label>
                    <input type="text" value={newAlert.description} onChange={(e) => setNewAlert({ ...newAlert, description: e.target.value })} placeholder="e.g., 'Take profit at $165'" style={{ width: '100%', padding: '6px', fontSize: '0.85rem', border: '1px solid #bfdbfe', borderRadius: '3px', boxSizing: 'border-box' }} />
                  </div>

                  <div style={{ display: 'flex', gap: '8px' }}>
                    <button onClick={handleAddAlert} style={{ flex: 1, padding: '6px', fontSize: '0.85rem', color: '#fff', background: '#0ea5e9', border: 'none', borderRadius: '3px', cursor: 'pointer', fontWeight: 500 }}>Add Criteria</button>
                    <button onClick={() => setIsAddingAlert(false)} style={{ flex: 1, padding: '6px', fontSize: '0.85rem', color: '#0c4a6e', background: 'transparent', border: '1px solid #bfdbfe', borderRadius: '3px', cursor: 'pointer' }}>Cancel</button>
                  </div>
                </div>
              )}
            </div>
          )}

          {/* Close Decision Form */}
          {isClosing && decision.status === 'open' && (
            <div style={{ background: '#fef2f2', padding: '14px', borderRadius: '6px', marginBottom: '16px', border: '1px solid #fee2e2' }}>
              <h3 style={{ fontSize: '0.9rem', fontWeight: 600, color: '#dc2626', margin: '0 0 12px 0' }}>Close Decision</h3>

              {/* Entry vs Exit Summary */}
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px', marginBottom: '12px' }}>
                <div style={{ padding: '10px', background: '#ffffff', borderRadius: '4px', border: '1px solid #fee2e2' }}>
                  <div style={{ fontSize: '0.7rem', color: '#9facbd', fontWeight: 600, marginBottom: '4px' }}>Entry Value</div>
                  <div style={{ fontSize: '0.9rem', fontWeight: 600, color: '#20242a' }}>
                    ${(decision.price * decision.quantity).toFixed(2)}
                  </div>
                  <div style={{ fontSize: '0.7rem', color: '#667085', marginTop: '2px' }}>
                    {decision.quantity} @ ${decision.price.toFixed(2)}
                  </div>
                </div>
                <div style={{ padding: '10px', background: '#ffffff', borderRadius: '4px', border: '1px solid #fee2e2' }}>
                  <div style={{ fontSize: '0.7rem', color: '#9facbd', fontWeight: 600, marginBottom: '4px' }}>Exit Value</div>
                  <div style={{ fontSize: '0.9rem', fontWeight: 600, color: '#20242a' }}>
                    {exitPrice ? `$${(parseFloat(exitPrice) * decision.quantity).toFixed(2)}` : '—'}
                  </div>
                  <div style={{ fontSize: '0.7rem', color: '#667085', marginTop: '2px' }}>
                    {exitPrice ? `${decision.quantity} @ $${parseFloat(exitPrice).toFixed(2)}` : 'Enter exit price'}
                  </div>
                </div>
              </div>

              {/* P/L Preview */}
              {exitPrice && parseFloat(exitPrice) > 0 && (() => {
                const pnl = (parseFloat(exitPrice) - decision.price) * decision.quantity;
                const pnlPct = ((parseFloat(exitPrice) - decision.price) / decision.price) * 100;
                return (
                  <div style={{
                    padding: '10px',
                    background: pnl >= 0 ? '#f0fdf4' : '#fef2f2',
                    border: `1px solid ${pnl >= 0 ? '#dcfce7' : '#fee2e2'}`,
                    borderRadius: '4px',
                    marginBottom: '12px'
                  }}>
                    <div style={{ fontSize: '0.7rem', color: '#667085', fontWeight: 600, marginBottom: '4px' }}>Final P/L</div>
                    <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                      <div style={{ fontSize: '0.9rem', fontWeight: 600, color: pnl >= 0 ? '#16a34a' : '#dc2626' }}>
                        {pnl >= 0 ? '+' : ''}${pnl.toFixed(2)}
                      </div>
                      <div style={{ fontSize: '0.9rem', fontWeight: 600, color: pnl >= 0 ? '#16a34a' : '#dc2626' }}>
                        {pnlPct >= 0 ? '+' : ''}{pnlPct.toFixed(1)}%
                      </div>
                    </div>
                  </div>
                );
              })()}

              {/* Exit Price Input */}
              <div style={{ marginBottom: '12px' }}>
                <label style={{ fontSize: '0.75rem', color: '#667085', fontWeight: 600, display: 'block', marginBottom: '4px' }}>
                  Exit Price (required)
                </label>
                <input
                  type="number"
                  step="0.01"
                  placeholder="0.00"
                  value={exitPrice}
                  onChange={(e) => setExitPrice(e.target.value)}
                  style={{
                    width: '100%',
                    padding: '8px 10px',
                    border: '1px solid #fee2e2',
                    borderRadius: '4px',
                    fontSize: '0.9rem',
                    boxSizing: 'border-box'
                  }}
                />
              </div>

              {/* Close Reason */}
              <div style={{ marginBottom: '12px' }}>
                <label style={{ fontSize: '0.75rem', color: '#667085', fontWeight: 600, display: 'block', marginBottom: '4px' }}>
                  Reason (optional)
                </label>
                <select
                  value={closeReason}
                  onChange={(e) => setCloseReason(e.target.value)}
                  style={{
                    width: '100%',
                    padding: '8px 10px',
                    border: '1px solid #fee2e2',
                    borderRadius: '4px',
                    fontSize: '0.9rem',
                    boxSizing: 'border-box'
                  }}
                >
                  <option value="">Select a reason...</option>
                  <option value="target_hit">Target hit</option>
                  <option value="stop_loss">Stop loss triggered</option>
                  <option value="thesis_broke">Thesis broke down</option>
                  <option value="opportunity_cost">Opportunity cost</option>
                  <option value="rebalancing">Portfolio rebalancing</option>
                  <option value="other">Other</option>
                </select>
              </div>

              {/* Action Buttons */}
              <div style={{ display: 'flex', gap: '8px' }}>
                <button
                  onClick={handleCloseDecision}
                  className="btn-primary"
                  style={{ flex: 1, background: '#dc2626' }}
                  disabled={isSaving}
                >
                  {isSaving ? 'Closing...' : '✓ Confirm Close'}
                </button>
                <button
                  onClick={() => setIsClosing(false)}
                  className="btn-secondary"
                  style={{ flex: 1 }}
                  disabled={isSaving}
                >
                  Cancel
                </button>
              </div>
            </div>
          )}

          {/* Edit History */}
          {decision.editHistory && decision.editHistory.length > 0 && (
            <div style={{ marginTop: '16px', paddingTop: '16px', borderTop: '1px solid #e4e7ec' }}>
              <h3 style={{ fontSize: '0.85rem', fontWeight: 600, color: '#667085', margin: '0 0 8px 0' }}>Edit History</h3>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
                {decision.editHistory.slice(0, 5).map((edit, idx) => (
                  <div key={idx} style={{ fontSize: '0.75rem', color: '#667085', padding: '6px 8px', background: '#f9fbfb', borderRadius: '4px' }}>
                    <div style={{ color: '#9facbd', fontSize: '0.7rem', marginBottom: '2px' }}>
                      {new Date(edit.editedAt).toLocaleString('en-US', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' })}
                    </div>
                    <div><strong>{edit.field}</strong>: {edit.newValue?.substring(0, 60)}{edit.newValue?.length > 60 ? '...' : ''}</div>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Action Buttons */}
          {!isClosing && (
            <div style={{ marginTop: '16px', paddingTop: '16px', borderTop: '1px solid #e4e7ec', display: 'flex', gap: '8px' }}>
              {decision.status === 'open' && !isEditing && (
                <>
                  <button
                    onClick={() => setIsEditing(true)}
                    className="btn-secondary"
                    style={{ flex: 1 }}
                  >
                    ✏️ Edit Decision
                  </button>
                  <button
                    onClick={() => setIsClosing(true)}
                    className="btn-secondary"
                    style={{ flex: 1, borderColor: '#dc2626', color: '#dc2626' }}
                  >
                    🔒 Close Decision
                  </button>
                </>
              )}
              {decision.status === 'closed' && (
                <div style={{
                  fontSize: '0.8rem',
                  color: '#9facbd',
                  padding: '8px',
                  background: '#f9fbfb',
                  borderRadius: '4px',
                  flex: 1,
                  textAlign: 'center',
                  fontStyle: 'italic'
                }}>
                  This decision is closed and read-only
                </div>
              )}
              <button
                onClick={onClose}
                className="btn-primary"
                style={{ flex: 1 }}
              >
                Done
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

function DecisionTable({ decisions, showEmpty = true }) {
  const getTypeColor = (type) => {
    switch (type) {
      case 'buy': return '#16a34a';
      case 'sell': return '#dc2626';
      case 'watch': return '#0f766e';
      case 'avoid': return '#ea580c';
      default: return '#9facbd';
    }
  };

  const getStatusColor = (status) => {
    switch (status) {
      case 'active': return '#16a34a';
      case 'closed': return '#9facbd';
      case 'archived': return '#d1d5db';
      default: return '#9facbd';
    }
  };

  return (
    <div className="decisionTable">
      {decisions.length > 0 && (
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr 1fr 1fr 1fr', gap: '8px', padding: '12px', background: '#f9fbfb', borderRadius: '6px', marginBottom: '8px', fontSize: '0.75rem', fontWeight: 600, color: '#667085', borderBottom: '1px solid #e4e7ec' }}>
          <span>Ticker</span>
          <span>Type</span>
          <span>Confidence</span>
          <span>Status</span>
          <span>Thesis</span>
          <span>Date</span>
        </div>
      )}
      {decisions.map((decision) => (
        <article
          className="tableRow"
          key={decision.id}
          style={{
            display: 'grid',
            gridTemplateColumns: '1fr 1fr 1fr 1fr 1fr 1fr',
            gap: '8px',
            alignItems: 'center',
            padding: '12px',
            borderBottom: '1px solid #e4e7ec',
            background: '#fff'
          }}
        >
          <strong style={{ fontSize: '0.95rem', color: '#20242a' }}>{decision.ticker}</strong>
          <span
            style={{
              display: 'inline-block',
              padding: '4px 8px',
              borderRadius: '4px',
              background: getTypeColor(decision.decisionType) + '20',
              color: getTypeColor(decision.decisionType),
              fontSize: '0.75rem',
              fontWeight: 600,
              textTransform: 'capitalize'
            }}
          >
            {decision.decisionType}
          </span>
          <span style={{ fontSize: '0.85rem', fontWeight: 600, color: '#20242a' }}>
            {decision.confidence}
            <span style={{ fontSize: '0.7rem', color: '#9facbd', fontWeight: 400 }}>/10</span>
          </span>
          <span
            style={{
              fontSize: '0.75rem',
              fontWeight: 500,
              color: getStatusColor(decision.status),
              textTransform: 'capitalize'
            }}
          >
            {decision.status}
          </span>
          <span style={{ fontSize: '0.8rem', color: '#667085', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
            {decision.title || '—'}
          </span>
          <small style={{ color: '#9facbd', fontSize: '0.75rem' }}>{formatDate(decision.createdAt)}</small>
        </article>
      ))}
      {showEmpty && decisions.length === 0 && <Empty text="No decisions to display." />}
    </div>
  );
}

function PerformanceSketch({ decisions, strategies, range }) {
  const count = Math.max(1, decisions.length + strategies.length);
  const path = `M 8 84 C 72 ${70 - count * 2}, 112 ${58 + count}, 172 ${44 - count} S 282 ${28 + count}, 360 ${34}`;
  return (
    <div className="chartShell">
      <svg viewBox="0 0 370 110" role="img" aria-label={`Performance overview for ${range}`}>
        <path d="M8 84 L360 84" className="baseline" />
        <path d={path} className="mainLine" />
        <path d="M8 88 C 100 80, 180 74, 360 62" className="benchLine" />
      </svg>
    </div>
  );
}

function Empty({ text }) {
  return <p className="empty">{text}</p>;
}

function recentActivity(decisions, reviews, strategies, publicProfile) {
  const items = [
    ...decisions.slice(0, 3).map((decision) => ({
      id: `decision-${decision.id}`,
      title: `Decision recorded: ${decision.ticker}`,
      detail: `${decision.decisionType} · confidence ${decision.confidence}`
    })),
    ...reviews.slice(0, 2).map((review) => ({
      id: `review-${review.id}`,
      title: `${review.reviewType} review scheduled`,
      detail: `Due ${review.dueDate}`
    })),
    ...strategies.slice(0, 2).map((strategy) => ({
      id: `strategy-${strategy.id}`,
      title: `Strategy created: ${strategy.name}`,
      detail: `${strategy.visibility} · ${currency(strategy.startingCapital)}`
    }))
  ];
  if (publicProfile) {
    items.unshift({ id: 'profile-live', title: 'Public profile live', detail: `@${publicProfile.handle}` });
  }
  return items.slice(0, 6);
}

function decisionFunnel(decisions, reviews, strategies) {
  const ideas = decisions.length;
  const researched = decisions.filter((decision) => decision.confidence >= 6).length;
  const active = decisions.filter((decision) => decision.status === 'active' || decision.status === 'closed').length;
  const positions = strategies.length;
  const reviewed = reviews.filter((review) => review.status === 'completed').length;
  const max = Math.max(ideas, researched, active, positions, reviewed, 1);
  return [
    ['Ideas captured', ideas],
    ['Researched', researched],
    ['Decisions made', active],
    ['Strategies opened', positions],
    ['Reviewed', reviewed]
  ].map(([label, value]) => ({ label, value, percent: Math.max(8, Math.round((value / max) * 100)) }));
}

function lines(value) {
  return value.split('\n').map((item) => item.trim()).filter(Boolean);
}

function commaList(value) {
  return value.split(',').map((item) => item.trim()).filter(Boolean);
}

function labelize(value) {
  return value.replace(/([A-Z])/g, ' $1').replace(/^./, (char) => char.toUpperCase());
}

function selectedMetricIds(reputation) {
  if (!reputation) return emptyProfile.publishedMetricIds;
  return [
    reputation.decisionQualityScore != null ? 'dqs' : null,
    reputation.researchDiscipline != null ? 'researchDiscipline' : null,
    reputation.riskManagement != null ? 'riskManagement' : null,
    reputation.strategyConsistency != null ? 'strategyConsistency' : null
  ].filter(Boolean);
}

function viewTitle(view) {
  return navItems.find(([id]) => id === view)?.[1] || 'Dashboard';
}

function scoreLabel(score) {
  if (score == null) return 'building baseline';
  if (score >= 80) return 'excellent';
  if (score >= 65) return 'good';
  if (score >= 50) return 'baseline';
  return 'needs attention';
}

function profileStatus(profile) {
  return profile ? 'Public profile live' : 'Private workspace';
}

function currency(value) {
  const amount = Number(value || 0);
  return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD', maximumFractionDigits: 0 }).format(amount);
}

function currencyPrecise(value) {
  if (value == null) return '—';
  return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD', maximumFractionDigits: 2 }).format(Number(value));
}

function signedCurrency(value) {
  if (value == null) return '—';
  const number = Number(value);
  const formatted = currencyPrecise(Math.abs(number));
  return `${number >= 0 ? '+' : '-'}${formatted}`;
}

function signedNumber(value) {
  if (value == null) return '—';
  const number = Number(value);
  return `${number >= 0 ? '+' : ''}${number.toFixed(2)}`;
}

function rangeLabel(value) {
  return {
    '1w': '1W',
    '1mo': '1M',
    '3mo': '3M',
    '6mo': '6M',
    '1y': '1Y',
    '2y': '2Y',
    '3y': '3Y',
    '4y': '4Y',
    '5y': '5Y'
  }[value] || value;
}

function percentLabel(value) {
  if (value == null) return 'No basis yet';
  const number = Number(value);
  return `${number >= 0 ? '+' : ''}${number.toFixed(2)}%`;
}

function signedPercentLabel(value) {
  if (value == null) return '—';
  const number = Number(value);
  return `${number >= 0 ? '+' : ''}${number.toFixed(2)}%`;
}

function accountTypeLabel(value) {
  const labels = {
    BROKERAGE: 'Brokerage',
    IRA: 'IRA',
    ROTH_IRA: 'Roth IRA',
    FOUR_O_ONE_K: '401K',
    HSA: 'HSA'
  };
  return labels[value] || labelize(String(value || 'Account'));
}

function formatDate(value) {
  if (!value) return 'not dated';
  return new Date(value).toLocaleDateString();
}

function generateMockCommunityProfiles(currentProfile) {
  const baseProfiles = [
    { handle: 'alex_trader', displayName: 'Alex Chen', bio: 'Tech sector focus. 5+ years experience.', dqs: 82, research: 78, risk: 85, consistency: 80 },
    { handle: 'jordan_growth', displayName: 'Jordan Silva', bio: 'Growth stock analyst. Contrarian thesis thinker.', dqs: 75, research: 88, risk: 72, consistency: 76 },
    { handle: 'morgan_value', displayName: 'Morgan Park', bio: 'Value investing disciplined approach', dqs: 88, research: 85, risk: 92, consistency: 87 },
    { handle: 'casey_quant', displayName: 'Casey Johnson', bio: 'Quantitative models + fundamental analysis', dqs: 79, research: 91, risk: 83, consistency: 85 },
    { handle: 'sam_dividend', displayName: 'Sam Rodriguez', bio: 'Income-focused portfolio builder', dqs: 71, research: 73, risk: 76, consistency: 72 }
  ];

  return baseProfiles
    .filter(p => !currentProfile || p.handle !== currentProfile.handle)
    .map(p => ({
      handle: p.handle,
      displayName: p.displayName,
      bio: p.bio,
      reputation: {
        decisionQualityScore: p.dqs,
        researchDiscipline: p.research,
        riskManagement: p.risk,
        strategyConsistency: p.consistency
      },
      publishedStrategies: { length: Math.floor(Math.random() * 4) + 1 }
    }));
}

function DecisionCaptureModal({
  isOpen,
  onClose,
  pendingDecision,
  formData,
  onFormChange,
  onSubmit,
  isSubmitting,
  hasSavedDraft
}) {
  const [optionalFieldsVisible, setOptionalFieldsVisible] = React.useState(true);
  const [thesisSuggestions, setThesisSuggestions] = React.useState([]);
  const [evidenceSuggestions, setEvidenceSuggestions] = React.useState([]);
  const [risksSuggestions, setRisksSuggestions] = React.useState([]);

  // Fetch suggestions from backend on mount
  React.useEffect(() => {
    const fetchSuggestions = async () => {
      try {
        const response = await fetch('/api/public/suggestions/all');
        if (response.ok) {
          const data = await response.json();
          setThesisSuggestions(data.thesis || []);
          setEvidenceSuggestions(data.evidence || []);
          setRisksSuggestions(data.risks || []);
        }
      } catch (error) {
        console.error('Failed to load suggestions:', error);
        // Fallback to defaults if API fails
        setThesisSuggestions([
          'Stock is undervalued (P/E or price/book below peers)',
          'Technical breakout signal (price breaks resistance)',
          'Momentum play (trend continuation)',
          'Mean reversion (oversold indicator like RSI < 30)',
          'Matches my investment strategy or watchlist criteria'
        ]);
        setEvidenceSuggestions([
          'P/E ratio below sector average',
          'RSI shows oversold conditions (< 30)',
          'Price above 50-day moving average',
          'Recent earnings beat or positive catalyst',
          'Sector/industry showing relative strength'
        ]);
        setRisksSuggestions([
          'Market downturn or sector correction',
          'Company earnings miss or guidance cut',
          'Sector rotation or fund flows shifting',
          'Valuation multiple compression',
          'Geopolitical or macro risk event'
        ]);
      }
    };

    if (isOpen) {
      fetchSuggestions();
    }
  }, [isOpen]);

  if (!isOpen || !pendingDecision) return null;

  const transactionTitle = `${pendingDecision.action} ${pendingDecision.shares} shares of ${pendingDecision.symbol} at $${pendingDecision.price?.toFixed(2) || '—'}`;
  const isAutoDecision = pendingDecision.isAuto;

  const handleCheckChange = (category, index) => {
    const key = `${category}Checked`;
    const current = formData[key] || [];
    const updated = current.includes(index)
      ? current.filter(i => i !== index)
      : [...current, index];
    onFormChange({ ...formData, [key]: updated });
  };

  const getSelectedSuggestions = (category, suggestions) => {
    const key = `${category}Checked`;
    return (formData[key] || []).map(i => suggestions[i]).filter(Boolean);
  };

  const combinedText = (category, suggestions) => {
    const selected = getSelectedSuggestions(category, suggestions);
    const custom = formData[category] || '';
    return [...selected, custom].filter(Boolean).join('; ');
  };

  const handleSubmitDecision = async (e) => {
    e.preventDefault();

    const thesis = combinedText('thesis', thesisSuggestions);
    const evidence = combinedText('evidence', evidenceSuggestions);
    const risks = combinedText('risks', risksSuggestions);
    const comments = formData.comments || '';

    const decisionData = {
      ...pendingDecision,
      thesis,
      evidence,
      risks,
      comments
    };

    // Clear draft on successful submit
    localStorage.removeItem('idp.decisionDraft');

    // Add flag for AUTO vs MANUAL to onSubmit handler
    await onSubmit(decisionData, isAutoDecision);
  };

  const handleLoadDraft = () => {
    const savedDraft = localStorage.getItem('idp.decisionDraft');
    if (savedDraft) {
      try {
        const draft = JSON.parse(savedDraft);
        onFormChange(draft);
      } catch (e) {
        // Invalid JSON
      }
    }
  };

  const handleClearDraft = () => {
    localStorage.removeItem('idp.decisionDraft');
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2>Log Investment Decision</h2>
          <button className="modal-close" onClick={onClose}>✕</button>
        </div>

        <div className="modal-body">
          <div className="decision-summary">
            <strong>{transactionTitle}</strong>
            {isAutoDecision && (
              <div style={{ fontSize: '12px', marginTop: '6px', color: '#13a79a', fontStyle: 'italic' }}>
                Price locked (real-time market price)
              </div>
            )}
          </div>

          {/* Draft Recovery & Optional Fields Toggle */}
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '12px', marginBottom: '12px', paddingBottom: '12px', borderBottom: '1px solid #e0e0e0' }}>
            <div style={{ display: 'flex', gap: '8px', alignItems: 'center' }}>
              {hasSavedDraft && (
                <>
                  <button
                    type="button"
                    onClick={handleLoadDraft}
                    className="btn-secondary"
                    style={{ fontSize: '12px', padding: '6px 10px' }}
                    title="Load previously saved draft"
                  >
                    📝 Load Draft
                  </button>
                  <button
                    type="button"
                    onClick={handleClearDraft}
                    className="btn-secondary"
                    style={{ fontSize: '12px', padding: '6px 10px', backgroundColor: '#f5f5f5' }}
                    title="Clear saved draft"
                  >
                    🗑️ Clear
                  </button>
                </>
              )}
            </div>
            <label style={{ fontSize: '13px', display: 'flex', alignItems: 'center', gap: '6px', cursor: 'pointer' }}>
              <input
                type="checkbox"
                checked={optionalFieldsVisible}
                onChange={(e) => setOptionalFieldsVisible(e.target.checked)}
                style={{ cursor: 'pointer' }}
              />
              Show optional fields
            </label>
          </div>

          <form onSubmit={handleSubmitDecision} className="decision-form">
            {/* Thesis Section */}
            <div className="form-section">
              <h3>Thesis <span className="label-optional">(optional)</span></h3>
              <div className="checkbox-group">
                {thesisSuggestions.map((suggestion, idx) => (
                  <label key={idx} className="checkbox-label">
                    <input
                      type="checkbox"
                      checked={(formData.thesisChecked || []).includes(idx)}
                      onChange={() => handleCheckChange('thesis', idx)}
                    />
                    {suggestion}
                  </label>
                ))}
              </div>
              <textarea
                placeholder="Add custom thesis..."
                value={formData.thesis || ''}
                onChange={(e) => onFormChange({ ...formData, thesis: e.target.value })}
                className="form-textarea"
                rows="2"
              />
            </div>

            {/* Evidence Section */}
            {optionalFieldsVisible && (
            <div className="form-section">
              <h3>Evidence <span className="label-optional">(optional)</span></h3>
              <div className="checkbox-group">
                {evidenceSuggestions.map((suggestion, idx) => (
                  <label key={idx} className="checkbox-label">
                    <input
                      type="checkbox"
                      checked={(formData.evidenceChecked || []).includes(idx)}
                      onChange={() => handleCheckChange('evidence', idx)}
                    />
                    {suggestion}
                  </label>
                ))}
              </div>
              <textarea
                placeholder="Add custom evidence..."
                value={formData.evidence || ''}
                onChange={(e) => onFormChange({ ...formData, evidence: e.target.value })}
                className="form-textarea"
                rows="2"
              />
            </div>
            )}

            {/* Risks Section */}
            {optionalFieldsVisible && (
            <div className="form-section">
              <h3>Risks <span className="label-optional">(optional)</span></h3>
              <div className="checkbox-group">
                {risksSuggestions.map((suggestion, idx) => (
                  <label key={idx} className="checkbox-label">
                    <input
                      type="checkbox"
                      checked={(formData.risksChecked || []).includes(idx)}
                      onChange={() => handleCheckChange('risks', idx)}
                    />
                    {suggestion}
                  </label>
                ))}
              </div>
              <textarea
                placeholder="Add custom risks..."
                value={formData.risks || ''}
                onChange={(e) => onFormChange({ ...formData, risks: e.target.value })}
                className="form-textarea"
                rows="2"
              />
            </div>
            )}

            {/* Exit Criteria Section */}
            <div className="form-section">
              <h3>Exit Criteria <span className="label-optional">(optional)</span></h3>
              <div className="exit-criteria-list">
                {(formData.exitCriteria || []).map((criteria, idx) => (
                  <div key={idx} className="exit-criteria-item">
                    <span className="exit-criteria-text">
                      {criteria.type === 'price_above' ? '↑ Price ≥' : criteria.type === 'price_below' ? '↓ Price ≤' : criteria.type === 'pnl_above' ? '↑ P/L ≥' : '↓ P/L ≤'}
                      {' '}${criteria.value} {criteria.description && `(${criteria.description})`}
                    </span>
                    <button
                      type="button"
                      onClick={() => {
                        const updated = formData.exitCriteria.filter((_, i) => i !== idx);
                        onFormChange({ ...formData, exitCriteria: updated });
                      }}
                      className="btn-remove"
                    >
                      ✕
                    </button>
                  </div>
                ))}
              </div>
              <div className="exit-criteria-input">
                <select
                  id="exitType"
                  className="form-select"
                  defaultValue="price_above"
                >
                  <option value="price_above">Price ≥ (Take Profit)</option>
                  <option value="price_below">Price ≤ (Stop Loss)</option>
                  <option value="pnl_above">P/L ≥ (Profit Target)</option>
                  <option value="pnl_below">P/L ≤ (Loss Limit)</option>
                </select>
                <input
                  type="number"
                  id="exitValue"
                  placeholder="Value"
                  className="form-input"
                  step="0.01"
                />
                <input
                  type="text"
                  id="exitDescription"
                  placeholder="Description (e.g., take profit)"
                  className="form-input"
                />
                <button
                  type="button"
                  onClick={() => {
                    const typeSelect = document.getElementById('exitType');
                    const valueInput = document.getElementById('exitValue');
                    const descInput = document.getElementById('exitDescription');

                    if (!valueInput.value) return;

                    const newCriteria = {
                      type: typeSelect.value,
                      value: Number(valueInput.value),
                      description: descInput.value || ''
                    };

                    const updated = [...(formData.exitCriteria || []), newCriteria];
                    onFormChange({ ...formData, exitCriteria: updated });

                    valueInput.value = '';
                    descInput.value = '';
                  }}
                  className="btn-secondary"
                  style={{ padding: '8px 12px', fontSize: '13px' }}
                >
                  + Add
                </button>
              </div>
            </div>

            {/* Comments Section */}
            {optionalFieldsVisible && (
            <div className="form-section">
              <h3>Comments <span className="label-optional">(optional)</span></h3>
              <textarea
                placeholder="Add any additional notes..."
                value={formData.comments || ''}
                onChange={(e) => onFormChange({ ...formData, comments: e.target.value })}
                className="form-textarea"
                rows="3"
              />
            </div>
            )}

            {/* Action Buttons */}
            <div className="modal-footer">
              <button type="button" onClick={onClose} className="btn-secondary">
                Skip for now
              </button>
              <button type="submit" disabled={isSubmitting} className="btn-primary">
                {isSubmitting ? 'Saving...' : 'Save Decision'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}

createRoot(document.getElementById('root')).render(<App />);
