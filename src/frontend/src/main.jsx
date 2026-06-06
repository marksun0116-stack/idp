import React, { useEffect, useMemo, useState } from 'react';
import { createRoot } from 'react-dom/client';
import { LineChart as RechartsLineChart, Line, BarChart as RechartsBarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import {
  Activity,
  BarChart3,
  BookOpen,
  Brain,
  CheckCircle2,
  Compass,
  Home,
  LineChart,
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
  startingCapital: 100000,
  visibility: 'private'
};

const emptyProfile = {
  handle: '',
  displayName: '',
  bio: '',
  publishedMetricIds: ['dqs', 'researchDiscipline', 'riskManagement', 'strategyConsistency']
};

const navItems = [
  ['dashboard', 'Dashboard', Home],
  ['decisions', 'Decisions', BookOpen],
  ['portfolios', 'Portfolios', WalletCards],
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
  const [strategyQuotes, setStrategyQuotes] = useState(null);
  const [strategyHistory, setStrategyHistory] = useState(null);
  const [indicator, setIndicator] = useState(null);
  const [publicProfile, setPublicProfile] = useState(null);
  const [decisionForm, setDecisionForm] = useState(emptyDecision);
  const [strategyForm, setStrategyForm] = useState(emptyStrategy);
  const [symbolForm, setSymbolForm] = useState({ symbol: '', note: '', tags: '', visibility: 'private' });
  const [profileForm, setProfileForm] = useState(emptyProfile);
  const [chartRange, setChartRange] = useState('1M');
  const [notice, setNotice] = useState('');
  const [loading, setLoading] = useState(false);
  const [showStrategyForm, setShowStrategyForm] = useState(false);
  const [showSymbolForm, setShowSymbolForm] = useState(false);

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
      setIndicator(null);
    }
  }, [selectedStrategyId]);

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
    setProfileForm(emptyProfile);
  }

  async function refreshWorkspace() {
    setLoading(true);
    setNotice('');
    try {
      const [decisionData, reviewData, dqsData, behaviorData, strategyData, profileData] = await Promise.all([
        api('/api/decisions'),
        api('/api/reviews'),
        api('/api/analytics/dqs'),
        api('/api/analytics/behavior'),
        api('/api/strategies'),
        api('/api/profile/public', { allowNotFound: true })
      ]);
      setDecisions(decisionData.decisions || []);
      setReviews(reviewData.reviews || []);
      setDqs(dqsData);
      setBehavior(behaviorData);
      setStrategies(strategyData.strategies || []);
      setPublicProfile(profileData);
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
      const quotes = await api(`/api/strategies/${strategyId}/quotes`);
      const history = await api(`/api/strategies/${strategyId}/history?range=1M`);
      setStrategyQuotes(quotes);
      setStrategyHistory(history);
      const firstSymbol = detail.trackedSymbols?.[0]?.symbol || quotes.symbols?.[0]?.symbol;
      if (firstSymbol) {
        const indicators = await api(`/api/strategies/${strategyId}/indicators?symbol=${firstSymbol}&range=1M`);
        setIndicator(indicators);
      }
      setIndicator(firstSymbol
        ? await api(`/api/strategies/${strategyId}/indicators?symbol=${encodeURIComponent(firstSymbol)}&range=1y`)
        : null);
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

  async function addSymbol(event) {
    event.preventDefault();
    if (!selectedStrategyId) return;
    setNotice('');
    try {
      await api(`/api/strategies/${selectedStrategyId}/symbols`, {
        method: 'POST',
        body: JSON.stringify({
          symbol: symbolForm.symbol.toUpperCase(),
          note: symbolForm.note,
          tags: commaList(symbolForm.tags),
          visibility: symbolForm.visibility
        })
      });
      setSymbolForm({ symbol: '', note: '', tags: '', visibility: 'private' });
      setNotice('Symbol added.');
      await loadStrategy(selectedStrategyId);
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
              strategyForm={strategyForm}
              setStrategyForm={setStrategyForm}
              createStrategy={createStrategy}
              symbolForm={symbolForm}
              setSymbolForm={setSymbolForm}
              addSymbol={addSymbol}
              strategyQuotes={strategyQuotes}
              strategyHistory={strategyHistory}
              indicator={indicator}
              chartRange={chartRange}
              setChartRange={setChartRange}
              showStrategyForm={showStrategyForm}
              setShowStrategyForm={setShowStrategyForm}
              showSymbolForm={showSymbolForm}
              setShowSymbolForm={setShowSymbolForm}
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
        <p>Track decisions, reviews, strategy portfolios, and public reputation signals from one private workspace.</p>
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
  setActiveView
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
            {['1M', '3M', 'YTD', '1Y', 'ALL'].map((range) => (
              <button className={chartRange === range ? 'selected' : ''} type="button" key={range} onClick={() => setChartRange(range)}>{range}</button>
            ))}
          </div>
          <PerformanceSketch decisions={decisions} strategies={strategies} range={chartRange} />
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
  return (
    <section className="workspaceGrid">
      <Panel title="Create Decision" icon={<Plus />}>
        <DecisionForm decisionForm={decisionForm} setDecisionForm={setDecisionForm} createDecision={createDecision} />
      </Panel>
      <Panel title="Decision Journal" icon={<BookOpen />}>
        <DecisionTable decisions={decisions} />
      </Panel>
    </section>
  );
}

function PortfolioSummary({ strategyQuotes, selectedStrategy }) {
  if (!strategyQuotes?.symbols || strategyQuotes.symbols.length === 0) {
    return <div style={{ fontSize: '0.9rem', color: '#667085', padding: '12px' }}>Add symbols to see portfolio metrics</div>;
  }

  const totalValue = strategyQuotes.symbols.reduce((sum, q) => sum + (q.lastPrice || 0), 0);
  const investedCapital = selectedStrategy?.startingCapital || 0;
  const gainLoss = totalValue - investedCapital;
  const gainLossPercent = investedCapital > 0 ? (gainLoss / investedCapital) * 100 : 0;
  const isPositive = gainLoss >= 0;

  return (
    <div style={{ padding: '12px' }}>
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '12px', marginBottom: '12px' }}>
        <div style={{ background: '#f9fbfb', padding: '10px', borderRadius: '7px', border: '1px solid #e2e8f0' }}>
          <small style={{ color: '#667085', fontSize: '0.75rem' }}>Total Value</small>
          <div style={{ fontSize: '1.3rem', fontWeight: 700, color: '#20242a' }}>
            ${totalValue.toFixed(0)}
          </div>
        </div>
        <div style={{ background: '#f9fbfb', padding: '10px', borderRadius: '7px', border: '1px solid #e2e8f0' }}>
          <small style={{ color: '#667085', fontSize: '0.75rem' }}>Invested</small>
          <div style={{ fontSize: '1rem', fontWeight: 600, color: '#526071' }}>
            ${investedCapital.toLocaleString()}
          </div>
        </div>
      </div>
      <div style={{ background: isPositive ? '#f0fdf4' : '#fef2f2', padding: '10px', borderRadius: '7px', border: `1px solid ${isPositive ? '#dcfce7' : '#fee2e2'}` }}>
        <small style={{ color: '#667085', fontSize: '0.75rem' }}>Unrealized P&L</small>
        <div style={{ fontSize: '1.2rem', fontWeight: 700, color: isPositive ? '#16a34a' : '#dc2626', display: 'flex', justifyContent: 'space-between', alignItems: 'baseline' }}>
          <span>${Math.abs(gainLoss).toFixed(0)}</span>
          <span style={{ fontSize: '0.9rem' }}>{isPositive ? '↑' : '↓'} {Math.abs(gainLossPercent).toFixed(1)}%</span>
        </div>
      </div>
      <div style={{ marginTop: '10px', fontSize: '0.8rem', color: '#667085', paddingTop: '10px', borderTop: '1px solid #e4e7ec' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '4px' }}>
          <span>{strategyQuotes.symbols.length} symbols tracked</span>
          <span>{strategyQuotes.symbols.filter(s => s.change > 0).length} gainers</span>
        </div>
      </div>
    </div>
  );
}

function AllocationChart({ strategyQuotes }) {
  if (!strategyQuotes?.symbols || strategyQuotes.symbols.length === 0) return null;

  const totalValue = strategyQuotes.symbols.reduce((sum, q) => sum + (q.lastPrice || 0), 0);
  const allocations = strategyQuotes.symbols.map(s => ({
    symbol: s.symbol,
    value: s.lastPrice || 0,
    percent: totalValue > 0 ? ((s.lastPrice || 0) / totalValue) * 100 : 0
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

function PriceChart({ strategyHistory, strategyQuotes, chartRange, setChartRange }) {
  if (!strategyHistory?.history || strategyHistory.history.length === 0 || !strategyQuotes?.symbols || strategyQuotes.symbols.length === 0) {
    return <div style={{ fontSize: '0.85rem', color: '#667085', padding: '12px' }}>Add symbols to view price history</div>;
  }

  const ranges = ['1D', '1W', '1M', '3M', '1Y', 'ALL'];
  const chartData = strategyHistory.history.map(point => ({
    date: new Date(point.timestamp).toLocaleDateString('en-US', { month: 'short', day: 'numeric' }),
    timestamp: point.timestamp,
    price: parseFloat(point.close) || 0
  }));

  const firstSymbol = strategyQuotes.symbols[0];
  const currentPrice = firstSymbol?.lastPrice || 0;
  const startPrice = chartData.length > 0 ? chartData[0].price : 0;
  const priceChange = currentPrice - startPrice;
  const priceChangePercent = startPrice > 0 ? (priceChange / startPrice) * 100 : 0;
  const isPositive = priceChange >= 0;

  return (
    <div style={{ padding: '12px' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline', marginBottom: '12px' }}>
        <div>
          <h3 style={{ margin: '0 0 4px 0', fontSize: '0.95rem', fontWeight: 600 }}>
            {firstSymbol?.symbol} Price History
          </h3>
          <small style={{ color: '#667085', fontSize: '0.75rem' }}>
            {chartData.length} data points
          </small>
        </div>
        <div style={{ textAlign: 'right' }}>
          <div style={{ fontSize: '1.3rem', fontWeight: 700, color: '#20242a' }}>
            ${currentPrice.toFixed(2)}
          </div>
          <small style={{ color: isPositive ? '#16a34a' : '#dc2626', fontWeight: 600 }}>
            {isPositive ? '↑' : '↓'} ${Math.abs(priceChange).toFixed(2)} ({isPositive ? '+' : ''}{priceChangePercent.toFixed(2)}%)
          </small>
        </div>
      </div>

      <div style={{ marginBottom: '12px', display: 'flex', gap: '6px', flexWrap: 'wrap' }}>
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
            {range}
          </button>
        ))}
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
              formatter={(value) => [`$${value.toFixed(2)}`, 'Price']}
              labelStyle={{ color: '#20242a' }}
            />
            <Line
              type="monotone"
              dataKey="price"
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
          <span>Range: {chartRange}</span>
          <span>Volatility: {Math.sqrt(chartData.reduce((sum, d, i, arr) => {
            if (i === 0) return 0;
            const prev = arr[i - 1].price;
            const curr = d.price;
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
  strategyForm,
  setStrategyForm,
  createStrategy,
  symbolForm,
  setSymbolForm,
  addSymbol,
  strategyQuotes,
  strategyHistory,
  indicator,
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
      <section className="workspaceGrid">
        <Panel title="Strategies" icon={<WalletCards />}>
          <div style={{ display: 'flex', gap: '8px', alignItems: 'center', marginBottom: '12px' }}>
            <div style={{ flex: 1 }}>
              <strong style={{ fontSize: '0.95rem' }}>{selectedStrategy.name}</strong>
              <p style={{ margin: '2px 0 0 0', fontSize: '0.8rem', color: '#667085' }}>{selectedStrategy.description}</p>
            </div>
            <span className="badge">{selectedStrategy.visibility}</span>
          </div>
          {showSymbolForm && (
            <form className="symbolForm" onSubmit={(e) => { addSymbol(e); setShowSymbolForm(false); }} style={{ display: 'grid', gridTemplateColumns: '1fr 1fr auto', gap: '8px', alignItems: 'flex-end', marginBottom: '12px', padding: '12px', background: '#f9fbfb', borderRadius: '7px', border: '1px solid #e4e7ec' }}>
              <Field label="Symbol" value={symbolForm.symbol} onChange={(value) => setSymbolForm({ ...symbolForm, symbol: value })} required />
              <Field label="Note" value={symbolForm.note} onChange={(value) => setSymbolForm({ ...symbolForm, note: value })} />
              <div style={{ display: 'flex', gap: '6px' }}>
                <button className="primary" type="submit" style={{ padding: '8px 12px' }}><Search size={16} />Track</button>
                <button type="button" onClick={() => setShowSymbolForm(false)} style={{ padding: '8px 12px', background: '#e4e7ec', border: 'none', borderRadius: '4px', cursor: 'pointer', fontSize: '0.85rem' }}>Cancel</button>
              </div>
            </form>
          )}
          {!showSymbolForm && (strategyQuotes?.symbols?.length || 0) >= 0 && (
            <button type="button" onClick={() => setShowSymbolForm(true)} style={{ width: '100%', padding: '8px', marginBottom: '12px', background: '#f0f0f0', border: '1px solid #e4e7ec', borderRadius: '4px', cursor: 'pointer', fontSize: '0.85rem', fontWeight: 500, color: '#526071' }}><Plus size={14} style={{ marginRight: '4px' }} />Add Symbol to Portfolio</button>
          )}
          <div className="strategyTabs">
            {strategies.map((strategy) => (
              <button type="button" className={strategy.id === selectedStrategyId ? 'selected' : ''} key={strategy.id} onClick={() => setSelectedStrategyId(strategy.id)}>
                {strategy.name}
              </button>
            ))}
          </div>
        </Panel>
        <Panel title="Create New Strategy" icon={<Plus />}>
          {showStrategyForm ? (
            <form className="stack" onSubmit={(e) => { createStrategy(e); setShowStrategyForm(false); }}>
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
                <button className="primary" type="submit"><Plus size={17} />Create Strategy</button>
                <button type="button" onClick={() => setShowStrategyForm(false)} style={{ padding: '8px 12px', background: '#e4e7ec', border: 'none', borderRadius: '4px', cursor: 'pointer', fontSize: '0.85rem' }}>Cancel</button>
              </div>
            </form>
          ) : (
            <button type="button" onClick={() => setShowStrategyForm(true)} style={{ width: '100%', padding: '12px', background: '#0f766e', color: '#fff', border: 'none', borderRadius: '4px', cursor: 'pointer', fontSize: '0.9rem', fontWeight: 600 }}><Plus size={16} style={{ marginRight: '6px' }} />New Strategy</button>
          )}
        </Panel>
      </section>
      <section className="workspaceGrid">
        <Panel title="Price History" icon={<LineChart />}>
          <PriceChart
            strategyHistory={strategyHistory}
            strategyQuotes={strategyQuotes}
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
            {(!strategyQuotes?.symbols || strategyQuotes.symbols.length === 0) && <Empty text="Add a tracked symbol to view prices." />}
          </div>
        </Panel>
      </section>
      <section className="workspaceGrid">
        <Panel title="Portfolio Summary" icon={<TrendingUp />}>
          <PortfolioSummary strategyQuotes={strategyQuotes} selectedStrategy={selectedStrategy} />
        </Panel>
        <Panel title="Asset Allocation" icon={<BarChart3 />}>
          <AllocationChart strategyQuotes={strategyQuotes} />
        </Panel>
      </section>
      <section className="workspaceGrid">
        <Panel title="Technical Indicators" icon={<ShieldCheck />}>
          <IndicatorPanel symbol={strategyQuotes?.symbols[0]?.symbol} indicator={indicator} />
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
  return (
    <form className="stack" onSubmit={createDecision}>
      <div className="row">
        <Field label="Ticker" value={decisionForm.ticker} onChange={(value) => setDecisionForm({ ...decisionForm, ticker: value })} required />
        <label>
          Type
          <select value={decisionForm.decisionType} onChange={(event) => setDecisionForm({ ...decisionForm, decisionType: event.target.value })}>
            <option value="watch">Watch</option>
            <option value="buy">Buy</option>
            <option value="sell">Sell</option>
            <option value="avoid">Avoid</option>
          </select>
        </label>
      </div>
      <Field label="Title" value={decisionForm.title} onChange={(value) => setDecisionForm({ ...decisionForm, title: value })} required />
      <TextField label="Thesis" value={decisionForm.thesis} onChange={(value) => setDecisionForm({ ...decisionForm, thesis: value })} required />
      <TextField label="Evidence" value={decisionForm.evidence} onChange={(value) => setDecisionForm({ ...decisionForm, evidence: value })} required />
      <TextField label="Risks" value={decisionForm.riskFactors} onChange={(value) => setDecisionForm({ ...decisionForm, riskFactors: value })} required />
      <div className="row">
        <label>
          Confidence
          <input type="number" min="1" max="10" value={decisionForm.confidence} onChange={(event) => setDecisionForm({ ...decisionForm, confidence: event.target.value })} />
        </label>
        <Field label="Horizon" value={decisionForm.timeHorizon} onChange={(value) => setDecisionForm({ ...decisionForm, timeHorizon: value })} required />
      </div>
      <TextField label="Exit Criteria" value={decisionForm.exitCriteria} onChange={(value) => setDecisionForm({ ...decisionForm, exitCriteria: value })} required />
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

function IndicatorPanel({ symbol, indicator }) {
  if (!indicator || !symbol) return <div style={{ fontSize: '0.85rem', color: '#667085' }}>Select a symbol for indicators</div>;

  const rsi = indicator.rsi14 || 0;
  const rsiStatus = rsi > 70 ? 'Overbought' : rsi < 30 ? 'Oversold' : 'Neutral';
  const rsiColor = rsi > 70 ? '#dc2626' : rsi < 30 ? '#16a34a' : '#9facbd';

  const trendVerdict = indicator.trendVerdict || 'No trend data';
  const confidence = indicator.confidence || 'N/A';
  const trendColor = trendVerdict.toLowerCase().includes('bearish') ? '#dc2626' : trendVerdict.toLowerCase().includes('bullish') ? '#16a34a' : '#9facbd';

  return (
    <div style={{ fontSize: '0.8rem' }}>
      <div style={{ marginBottom: '8px', paddingBottom: '6px', borderBottom: '1px solid #e4e7ec' }}>
        <strong style={{ display: 'flex', justifyContent: 'space-between' }}>
          <span>RSI(14)</span>
          <span style={{ color: rsiColor, fontWeight: 700 }}>{rsi.toFixed(1)}</span>
        </strong>
        <small style={{ color: '#667085' }}>{rsiStatus}</small>
      </div>
      <div style={{ marginBottom: '8px', paddingBottom: '6px', borderBottom: '1px solid #e4e7ec' }}>
        <strong style={{ display: 'flex', justifyContent: 'space-between' }}>
          <span>Trend</span>
          <span style={{ color: trendColor, fontWeight: 700 }}>{trendVerdict.toUpperCase()}</span>
        </strong>
        <small style={{ color: '#667085' }}>Confidence: {confidence}</small>
      </div>
      {indicator.sampleSize && (
        <div style={{ marginTop: '6px', padding: '6px', background: '#fafbfc', borderLeft: '3px solid #9facbd', borderRadius: '3px' }}>
          <small style={{ color: '#4b5563' }}>
            Based on {indicator.sampleSize} trading days (Range: {indicator.range})
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

function Field({ label, value, onChange, required }) {
  return (
    <label>
      {label}
      <input value={value} required={required} onChange={(event) => onChange(event.target.value)} />
    </label>
  );
}

function TextField({ label, value, onChange, required }) {
  return (
    <label>
      {label}
      <textarea value={value} required={required} onChange={(event) => onChange(event.target.value)} />
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

function DecisionTable({ decisions }) {
  return (
    <div className="decisionTable">
      {decisions.map((decision) => (
        <article className="tableRow" key={decision.id}>
          <strong>{decision.ticker}</strong>
          <span>{decision.decisionType}</span>
          <span>{decision.confidence}/10</span>
          <span>{decision.status}</span>
          <small>{formatDate(decision.createdAt)}</small>
        </article>
      ))}
      {decisions.length === 0 && <Empty text="No decisions yet." />}
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

createRoot(document.getElementById('root')).render(<App />);
