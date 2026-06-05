// ─────────────────────────────────────────────────────────────
// Dashboard variants — three layouts over one salaried engine
//   A · Editorial   B · Gauge-centric   C · Stacked cards
// The waterfall:  income − asides = spendable − expenses = balance
// ─────────────────────────────────────────────────────────────

// shared header: greeting + daily/weekly switch + theme + settings
function DashHeader({ name, view, setView, dark, onToggleTheme, onOpenSettings }) {
  const hour = 19; // fixed "evening" for the mock
  const greet = hour < 12 ? 'Good morning' : hour < 18 ? 'Good afternoon' : 'Good evening';
  return (
    <div className="dash-head">
      <div>
        <div className="t-label-s" style={{ color: 'var(--on-variant)', letterSpacing: '.16em' }}>{greet.toUpperCase()}</div>
        <div className="t-title-l emph" style={{ color: 'var(--on-surface)' }}>{name}</div>
      </div>
      <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
        <Segmented small value={view} onChange={setView}
        options={[{ value: 'daily', label: 'Daily' }, { value: 'weekly', label: 'Weekly' }]} />
        <IconButton label="Toggle theme" onClick={onToggleTheme}>{dark ? Icon.sun : Icon.moon}</IconButton>
        <IconButton label="Settings" onClick={onOpenSettings}>{Icon.gear}</IconButton>
      </div>
    </div>);

}

// slim cycle progress bar
function CycleBar({ cyc, tight }) {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
      <div className="cyc-track">
        <div className="cyc-fill" style={{ width: `${cyc.progress * 100}%`, background: tight ? 'var(--status)' : 'var(--primary)' }} />
      </div>
      <div style={{ display: 'flex', justifyContent: 'space-between', gap: 10 }}>
        <span className="t-label-m" style={{ color: 'var(--on-variant)', letterSpacing: '.08em', whiteSpace: 'nowrap' }}>CYCLE · {cycleLabel(cyc)}</span>
        <span className="t-label-m" style={{ color: 'var(--on-variant)', letterSpacing: '.06em', whiteSpace: 'nowrap' }}>{cyc.remaining} DAYS LEFT</span>
      </div>
    </div>);

}

function CatDot({ cat, seedHue, dark, size = 10 }) {
  return <span style={{ width: size, height: size, borderRadius: 99, background: catColor(cat, seedHue, dark), flexShrink: 0, display: 'inline-block' }} />;
}

// ── the "balanced view": one income bar split into asides + expenses + free ──
function AllocationBar({ metrics, seedHue, dark, height = 16 }) {
  const inc = metrics.income;
  // collect from actual transaction categories, ordered: asides first, then expenses
  const cats = Object.keys(metrics.byCategory);
  const asides = cats.filter((c) => catKind(c, metrics.txns.find(tx => tx.category === c)?.kind) === 'aside');
  const expenses = cats.filter((c) => catKind(c, metrics.txns.find(tx => tx.category === c)?.kind) === 'expense');
  const segs = [...asides, ...expenses]
    .map((c) => ({ cat: c, value: metrics.byCategory[c] }))
    .filter((s) => s.value > 0);
  const used = metrics.allocated + metrics.spent;
  const remaining = Math.max(0, inc - used);
  const over = used > inc;
  return (
    <div className="alloc-bar" style={{ height }}>
      {segs.map((s) => (
        <div key={s.cat} className="alloc-seg"
          style={{ width: `${Math.max(0, (over ? s.value / used : s.value / inc)) * 100}%`, background: catColor(s.cat, seedHue, dark) }}
          title={`${catLabel(s.cat)} · ${money(s.value)}`} />
      ))}
      {!over && remaining > 0 && <div className="alloc-rest" style={{ width: `${remaining / inc * 100}%` }} title={`Unspent · ${money(remaining)}`} />}
    </div>);

}

// ── the waterfall figures, as an editorial list ──
function WaterfallRows({ metrics, compact }) {
  const rows = [
    { label: 'Income', value: metrics.income, op: '' },
    { label: 'Set aside', value: metrics.allocated, op: '−', dim: true },
    { label: 'Spendable', value: metrics.spendable, op: '=', rule: true },
    { label: 'Spent', value: metrics.spent, op: '−', dim: true },
  ];
  return (
    <div className="wf">
      {rows.map((r) => (
        <div key={r.label} className={'wf-row' + (r.rule ? ' wf-rule' : '')}>
          <span className="t-body-l" style={{ color: r.dim ? 'var(--on-variant)' : 'var(--on-surface)' }}>
            <span className="wf-op">{r.op}</span>{r.label}
          </span>
          <span className={'t-body-l' + (r.rule ? ' emph' : '')} style={{ color: r.dim ? 'var(--on-variant)' : 'var(--on-surface)' }}>
            {r.op === '−' ? '− ' : ''}{money(r.value)}
          </span>
        </div>
      ))}
    </div>);

}

// ── closed-cycle outcome chip (surplus / borrowed) ──
function OutcomeNote({ metrics }) {
  if (!metrics.ended) return null;
  const surplus = metrics.balance >= 0;
  return (
    <div className="outcome" data-pos={surplus ? '' : undefined}>
      <span className="t-label-m" style={{ letterSpacing: '.14em' }}>{surplus ? 'CYCLE CLOSED · SURPLUS' : 'CYCLE CLOSED · BORROWED'}</span>
      <span className="t-title-l emph">{surplus ? '+' : '−'}{money(Math.abs(metrics.balance))}</span>
    </div>);

}

// ── weekly aggregation for the "Weekly" view ──
function groupByWeek(txns) {
  const map = new Map();
  txns.forEach((tx) => {
    const d = new Date(tx.date + 'T00:00:00');
    const day = (d.getDay() + 6) % 7; // Monday = 0
    const monday = new Date(d.getTime() - day * 86400000);
    const key = monday.toISOString().slice(0, 10);
    if (!map.has(key)) map.set(key, { date: key, monday, items: [], total: 0 });
    const g = map.get(key);g.items.push(tx);g.total += tx.amount;
  });
  return [...map.values()].sort((a, b) => a.date < b.date ? 1 : -1).map((g) => ({
    ...g,
    label: 'WEEK OF ' + g.monday.toLocaleDateString('en-GB', { day: 'numeric', month: 'short' }).toUpperCase()
  }));
}

// shared timeline used by every variant
function Timeline({ metrics, view, seedHue, dark, highlightId, compact }) {
  const today = new Date(2026, 5, 4);
  const groups = view === 'weekly' ? groupByWeek(metrics.txns) : groupByDay(metrics.txns, today);
  return (
    <div className="timeline">
      {groups.map((g) =>
      <div key={g.date} className="tl-group">
          <div className="tl-divider">
            <span className="t-label-m" style={{ color: 'var(--on-surface)', letterSpacing: '.16em' }}>{g.label}</span>
            <span className="t-label-m" style={{ color: 'var(--on-variant)' }}>{money(g.total, 2)}</span>
          </div>
          {g.items.map((tx) =>
        <div key={tx.id} className={'tl-row kin-row' + (tx.id === highlightId ? ' tl-new' : '')}
        style={compact ? { paddingTop: 10, paddingBottom: 10 } : undefined}>
              <CatDot cat={tx.category} seedHue={seedHue} dark={dark} />
              <div style={{ flex: 1, minWidth: 0 }}>
                <div className="t-body-l" style={{ color: 'var(--on-surface)' }}>{tx.note}</div>
                <div className="t-label-s" style={{ color: 'var(--on-variant)', letterSpacing: '.1em', textTransform: 'uppercase' }}>
                  {catLabel(tx.category)}{catKind(tx.category) === 'aside' && ' · set aside'}
                </div>
              </div>
              <div className="t-title-l emph" style={{ color: catKind(tx.category) === 'aside' ? 'var(--on-variant)' : 'var(--on-surface)' }}>
                {catKind(tx.category) === 'aside' ? '↓ ' : ''}{money(tx.amount, 2)}
              </div>
            </div>
        )}
        </div>
      )}
      <div style={{ height: 120 }} />
    </div>);

}

// ════════════════════════════ A · EDITORIAL ════════════════════════════
function DashEditorial(props) {
  const { metrics, seedHue, dark } = props;
  const sp = moneyParts(metrics.safeToSpend);
  return (
    <>
      <DashHeader {...props} />
      <div className="scroll-pad">
        <div className="ed-hero hero-tap" onClick={props.onOpenCycle} role="button" aria-label="View cycle detail">
          <span className="hero-chev">{Icon.chev}</span>
          {metrics.ended ?
          <>
              <div className="t-label-m" style={{ color: 'var(--on-variant)', letterSpacing: '.18em' }}>{metrics.balance >= 0 ? 'CLOSED WITH SURPLUS' : 'CLOSED · BORROWED'}</div>
              <div className="ed-num" style={{ color: metrics.balance >= 0 ? 'var(--on-surface)' : 'var(--status)' }}>
                <span className="ed-cur">£</span>{moneyParts(Math.abs(metrics.balance)).whole}
              </div>
              <div className="t-label-m" style={{ color: 'var(--on-variant)', letterSpacing: '.18em' }}>{cycleLabel(metrics.cyc)}</div>
            </> :

          <>
              <div className="t-label-m" style={{ color: 'var(--on-variant)', letterSpacing: '.18em' }}>SAFE TO SPEND</div>
              <div className="ed-num" style={{ color: metrics.tight ? 'var(--status)' : 'var(--on-surface)' }}>
                <span className="ed-cur">£</span>{sp.whole}<span className="ed-frac">.{sp.frac}</span>
              </div>
              <div className="t-label-m" style={{ color: 'var(--on-variant)', letterSpacing: '.18em' }}>PER DAY · FOR {metrics.cyc.remaining} DAYS</div>
            </>
          }
        </div>

        <div style={{ padding: '6px 24px 10px' }}>
          <AllocationBar metrics={metrics} seedHue={seedHue} dark={dark} />
        </div>

        <div className="ed-sub">
          <div>
            <div className="t-label-s" style={{ color: 'var(--on-variant)', letterSpacing: '.14em' }}>SPENDABLE</div>
            <div className="t-headline-s emph" style={{ color: 'var(--on-surface)' }}>{money(metrics.spendable)}</div>
          </div>
          <div style={{ textAlign: 'right' }}>
            <div className="t-label-s" style={{ color: 'var(--on-variant)', letterSpacing: '.14em' }}>{metrics.balance >= 0 ? 'BALANCE' : 'OVER BY'}</div>
            <div className="t-headline-s emph" style={{ color: metrics.balance >= 0 ? 'var(--on-surface)' : 'var(--status)' }}>{money(Math.abs(metrics.balance))}</div>
          </div>
        </div>

        <div style={{ padding: '0 24px 22px' }}><CycleBar cyc={metrics.cyc} tight={metrics.tight} /></div>
        <Timeline {...props} highlightId={props.highlightId} />
      </div>
    </>);

}

// ════════════════════════════ B · GAUGE ════════════════════════════
function DashGauge(props) {
  const { metrics, seedHue, dark } = props;
  const sp = moneyParts(metrics.safeToSpend);
  // fraction of the spendable pot still unspent
  const frac = Math.max(0, Math.min(1, metrics.spendable > 0 ? metrics.balance / metrics.spendable : 0));
  return (
    <>
      <DashHeader {...props} />
      <div className="scroll-pad">
        <div className={'gauge-card hero-tap' + (metrics.tight ? ' sharp' : '')} onClick={props.onOpenCycle} role="button" aria-label="View cycle detail">
          <span className="hero-chev">{Icon.chev}</span>
          <RingGauge value={frac} size={236} stroke={20}
          sharp={metrics.tight}
          color={metrics.tight ? 'var(--status)' : 'var(--primary)'}
          track={dark ? 'var(--sc-high)' : 'var(--sc-highest)'}>
            <div className="t-label-m" style={{ color: 'var(--on-variant)', letterSpacing: '.16em' }}>SAFE / DAY</div>
            <div className="g-num" style={{ color: metrics.tight ? 'var(--status)' : 'var(--on-surface)' }}>
              <span style={{ fontSize: '.5em', verticalAlign: '0.55em' }}>£</span>{sp.whole}
            </div>
            <div className="t-label-s" style={{ color: 'var(--on-variant)', letterSpacing: '.14em' }}>{money(metrics.balance)} of {money(metrics.spendable)}</div>
          </RingGauge>
          <div className="g-legend">
            <div className="g-leg-item">
              <span style={{ width: 9, height: 9, borderRadius: 99, background: 'var(--on-variant)', opacity: .4 }} />
              <span className="t-label-s" style={{ color: 'var(--on-variant)', textTransform: 'uppercase', letterSpacing: '.1em' }}>Set aside</span>
              <span className="t-body-l emph" style={{ color: 'var(--on-surface)' }}>{money(metrics.allocated)}</span>
            </div>
            <div className="g-leg-item">
              <CatDot cat="essentials" seedHue={seedHue} dark={dark} size={9} />
              <span className="t-label-s" style={{ color: 'var(--on-variant)', textTransform: 'uppercase', letterSpacing: '.1em' }}>Spent</span>
              <span className="t-body-l emph" style={{ color: 'var(--on-surface)' }}>{money(metrics.spent)}</span>
            </div>
            <div className="g-leg-item">
              <span style={{ width: 9, height: 9, borderRadius: 99, border: '1.5px solid var(--outline)' }} />
              <span className="t-label-s" style={{ color: 'var(--on-variant)', textTransform: 'uppercase', letterSpacing: '.1em' }}>{metrics.balance >= 0 ? 'Balance' : 'Over by'}</span>
              <span className="t-body-l emph" style={{ color: metrics.balance >= 0 ? 'var(--on-surface)' : 'var(--status)' }}>{money(Math.abs(metrics.balance))}</span>
            </div>
          </div>
        </div>
        <Timeline {...props} highlightId={props.highlightId} compact />
      </div>
    </>);

}

// ════════════════════════════ C · CARDS ════════════════════════════
function StepCard({ label, value, accent, dark, kind }) {
  return (
    <div className="bk-card">
      <div style={{ display: 'flex', alignItems: 'center', gap: 7 }}>
        <span style={{ width: 9, height: 9, borderRadius: 99, background: accent, flexShrink: 0 }} />
        <span className="t-label-s" style={{ color: 'var(--on-variant)', textTransform: 'uppercase', letterSpacing: '.1em' }}>{label}</span>
      </div>
      <div className="t-title-l emph" style={{ color: 'var(--on-surface)' }}>{value}</div>
    </div>);

}

function DashCards(props) {
  const { metrics, seedHue, dark } = props;
  const sp = moneyParts(metrics.safeToSpend);
  return (
    <>
      <DashHeader {...props} />
      <div className="scroll-pad">
        <div className={'cards-hero hero-tap' + (metrics.tight ? ' sharp' : '')} onClick={props.onOpenCycle} role="button" aria-label="View cycle detail">
          <span className="hero-chev" style={{ color: 'var(--on-primary-container)' }}>{Icon.chev}</span>
          <div className="t-label-m" style={{ color: 'var(--on-primary-container)', letterSpacing: '.18em' }}>
            {metrics.ended ? (metrics.balance >= 0 ? 'CLOSED · SURPLUS' : 'CLOSED · BORROWED') : 'SAFE TO SPEND · PER DAY'}
          </div>
          <div className="ch-num" style={{ color: 'var(--on-primary-container)' }}>
            <span style={{ fontSize: '.46em', verticalAlign: '0.6em' }}>£</span>
            {metrics.ended ? moneyParts(Math.abs(metrics.balance)).whole : <>{sp.whole}<span style={{ fontSize: '.42em', opacity: .7 }}>.{sp.frac}</span></>}
          </div>
          <div style={{ marginTop: 4 }}>
            <AllocationBar metrics={metrics} seedHue={seedHue} dark={dark} height={12} />
          </div>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end', marginTop: 10 }}>
            <span className="t-label-s" style={{ color: 'var(--on-primary-container)', opacity: .85, letterSpacing: '.12em' }}>{cycleLabel(metrics.cyc)}</span>
            <span className="t-body-l emph" style={{ color: 'var(--on-primary-container)' }}>{money(metrics.balance)} {metrics.balance >= 0 ? 'left' : 'over'}</span>
          </div>
        </div>
        <div className="bk-row">
          <StepCard label="Income" value={money(metrics.income)} accent="var(--on-variant)" dark={dark} />
          <StepCard label="Set aside" value={money(metrics.allocated)} accent={catColor('savings', seedHue, dark)} dark={dark} />
          <StepCard label="Spent" value={money(metrics.spent)} accent={catColor('lifestyle', seedHue, dark)} dark={dark} />
        </div>
        <div style={{ padding: '4px 20px 18px' }}><CycleBar cyc={metrics.cyc} tight={metrics.tight} /></div>
        <Timeline {...props} highlightId={props.highlightId} compact />
      </div>
    </>);

}

Object.assign(window, { DashEditorial, DashGauge, DashCards, Timeline, AllocationBar, WaterfallRows, CatDot, OutcomeNote });
