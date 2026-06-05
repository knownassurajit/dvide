// ─────────────────────────────────────────────────────────────
// App root — token application, scaling stage, state, tweaks
// ─────────────────────────────────────────────────────────────

const TODAY = new Date(2026, 5, 4); // fixed "now" for the mock — 4 Jun 2026

// ── sample ledgers — each entry is either an `aside` (kept) or `expense` (spent) ──
// HEALTHY: salary set aside early, light spending, comfortable runway
const DATA_HEALTHY = {
  income: 3200, anchorDay: 25,
  transactions: [
    { id: 1, date: '2026-05-25', category: 'savings',    amount: 400.00, note: 'Emergency fund' },
    { id: 2, date: '2026-05-25', category: 'investment', amount: 300.00, note: 'Index fund · SIP' },
    { id: 3, date: '2026-05-26', category: 'security',   amount: 150.00, note: 'Health insurance' },
    { id: 4, date: '2026-05-26', category: 'essentials', amount: 48.20,  note: 'Groceries · Sainsbury\u2019s' },
    { id: 5, date: '2026-05-28', category: 'lifestyle',  amount: 34.00,  note: 'Dinner · Otto\u2019s' },
    { id: 6, date: '2026-06-01', category: 'essentials', amount: 18.40,  note: 'Pharmacy' },
    { id: 7, date: '2026-06-03', category: 'lifestyle',  amount: 4.80,   note: 'Flat white' },
  ],
};
// TIGHT: less set aside, heavy spending — daily allowance squeezed
const DATA_TIGHT = {
  income: 3200, anchorDay: 25,
  transactions: [
    { id: 1, date: '2026-05-25', category: 'savings',    amount: 200.00, note: 'Emergency fund' },
    { id: 2, date: '2026-05-25', category: 'security',   amount: 150.00, note: 'Insurance' },
    { id: 3, date: '2026-05-25', category: 'essentials', amount: 1450.00, note: 'Rent' },
    { id: 4, date: '2026-05-26', category: 'essentials', amount: 96.30,  note: 'Groceries · big shop' },
    { id: 5, date: '2026-05-27', category: 'lifestyle',  amount: 180.00, note: 'Concert tickets' },
    { id: 6, date: '2026-05-29', category: 'lifestyle',  amount: 240.00, note: 'New jacket' },
    { id: 7, date: '2026-05-31', category: 'essentials', amount: 72.10,  note: 'Groceries' },
    { id: 8, date: '2026-06-02', category: 'lifestyle',  amount: 88.00,  note: 'Dinner · Bart\u00f3k' },
    { id: 9, date: '2026-06-03', category: 'lifestyle',  amount: 26.00,  note: 'Rideshare' },
  ],
};
// OVERSPENT: a closed cycle that ran past its spendable — settles as borrowed
const DATA_OVERSPENT = {
  income: 3200, anchorDay: 25,
  transactions: [
    { id: 1, date: '2026-05-25', category: 'savings',    amount: 300.00, note: 'Emergency fund' },
    { id: 2, date: '2026-05-25', category: 'investment', amount: 200.00, note: 'Index fund · SIP' },
    { id: 3, date: '2026-05-25', category: 'security',   amount: 150.00, note: 'Insurance' },
    { id: 4, date: '2026-05-25', category: 'essentials', amount: 1500.00, note: 'Rent' },
    { id: 5, date: '2026-05-27', category: 'essentials', amount: 260.00, note: 'Groceries · month' },
    { id: 6, date: '2026-05-28', category: 'essentials', amount: 180.00, note: 'Utilities' },
    { id: 7, date: '2026-05-30', category: 'lifestyle',  amount: 360.00, note: 'Birthday weekend' },
    { id: 8, date: '2026-06-05', category: 'lifestyle',  amount: 320.00, note: 'New phone' },
    { id: 9, date: '2026-06-10', category: 'lifestyle',  amount: 170.00, note: 'Concert + dinner' },
  ],
};

// scenario → dataset + whether the cycle has closed
const SCENARIOS = {
  healthy:  { data: DATA_HEALTHY,   ended: false },
  tight:    { data: DATA_TIGHT,     ended: false },
  surplus:  { data: DATA_HEALTHY,   ended: true },
  borrowed: { data: DATA_OVERSPENT, ended: true },
};

// closed cycles for the archive list (most recent first)
const ARCHIVE = [
  { label: 'May 2026', range: '25 Apr – 24 May', balance: 312 },
  { label: 'April 2026', range: '25 Mar – 24 Apr', balance: 86 },
  { label: 'March 2026', range: '25 Feb – 24 Mar', balance: -47 },
  { label: 'February 2026', range: '25 Jan – 24 Feb', balance: 204 },
];

const SEEDS = [
  { name: 'Violet', hue: 300 },
  { name: 'Indigo', hue: 265 },
  { name: 'Forest', hue: 152 },
  { name: 'Clay', hue: 38 },
];

const TWEAK_DEFAULTS = /*EDITMODE-BEGIN*/{
  "variant": "editorial",
  "dark": true,
  "seed": 300,
  "spending": "healthy",
  "motion": "full"
}/*EDITMODE-END*/;

const VARIANTS = [
  { value: 'editorial', label: 'Editorial', cmp: 'DashEditorial' },
  { value: 'gauge', label: 'Gauge', cmp: 'DashGauge' },
  { value: 'cards', label: 'Cards', cmp: 'DashCards' },
];

function applyRoles(el, roles) {
  const map = {
    surface: '--surface', surfaceDim: '--surface-dim',
    surfaceContainerLowest: '--sc-lowest', surfaceContainerLow: '--sc-low',
    surfaceContainer: '--sc', surfaceContainerHigh: '--sc-high', surfaceContainerHighest: '--sc-highest',
    onSurface: '--on-surface', onSurfaceVariant: '--on-variant',
    outline: '--outline', outlineFaint: '--outline-faint',
    primary: '--primary', onPrimary: '--on-primary',
    primaryContainer: '--primary-container', onPrimaryContainer: '--on-primary-container',
    status: '--status', statusContainer: '--status-container', scrim: '--scrim',
  };
  Object.entries(map).forEach(([k, v]) => el.style.setProperty(v, roles[k]));
}

function Stage({ children }) {
  const [scale, setScale] = useState(1);
  useEffect(() => {
    const fit = () => {
      const availH = window.innerHeight - 96;
      const availW = window.innerWidth - 32;
      setScale(Math.min(1, availH / 874, availW / 402));
    };
    fit();
    window.addEventListener('resize', fit);
    return () => window.removeEventListener('resize', fit);
  }, []);
  return (
    <div style={{ width: 402 * scale, height: 874 * scale, position: 'relative' }}>
      <div style={{ width: 402, height: 874, position: 'absolute', top: 0, left: 0, transformOrigin: 'top left', transform: `scale(${scale})` }}>
        {children}
      </div>
    </div>
  );
}

function App() {
  const [t, setTweak] = useTweaks(TWEAK_DEFAULTS);
  const rmSystem = useReducedMotion();
  const rm = rmSystem || t.motion === 'calm';

  const [view, setView] = useState('daily');
  const scn = SCENARIOS[t.spending] || SCENARIOS.healthy;
  const [data, setData] = useState({ ...scn.data, ended: scn.ended });
  const [highlightId, setHighlightId] = useState(null);
  const [open, setOpen] = useState(false);
  const [screen, setScreen] = useState(null); // null | 'settings' | 'cycle'

  // swap dataset when the spending-state scenario changes
  useEffect(() => {
    const s = SCENARIOS[t.spending] || SCENARIOS.healthy;
    setData({ ...s.data, ended: s.ended });
  }, [t.spending]);

  const rootRef = useRef(null);
  const roles = buildRoles(t.seed, t.dark);
  useEffect(() => { if (rootRef.current) applyRoles(rootRef.current, roles); });

  const metrics = computeMetrics(data, TODAY);
  const scrollRef = useKineticScroll(!rm);

  const addTxn = ({ amount, category, kind, note }) => {
    const id = Math.max(0, ...data.transactions.map((x) => x.id)) + 1;
    const tx = { id, date: '2026-06-04', category, kind, amount, note };
    setData((d) => ({ ...d, transactions: [tx, ...d.transactions] }));
    setHighlightId(id);
    setTimeout(() => setHighlightId(null), 1400);
  };

  const Variant = window[VARIANTS.find((v) => v.value === t.variant).cmp];
  const dashProps = {
    metrics, view, setView, seedHue: t.seed, dark: t.dark, income: data.income,
    name: 'Sam', highlightId, onToggleTheme: () => setTweak('dark', !t.dark),
    onOpenSettings: () => { setScreen('settings'); haptic('light'); },
    onOpenCycle: () => { setScreen('cycle'); haptic('light'); },
  };

  return (
    <div className="page">
      {/* variant switcher — outside the scaled frame, always reachable */}
      <div className="switcher">
        {VARIANTS.map((v) => (
          <button key={v.value} className="sw-btn" data-on={t.variant === v.value ? '' : undefined}
            onClick={() => setTweak('variant', v.value)}>{v.label}</button>
        ))}
      </div>

      <Stage>
        <IOSDevice dark={t.dark} width={402} height={874}>
          <div className="app-root" ref={rootRef} data-dark={t.dark ? '' : undefined}
            style={{ background: 'var(--surface)', color: 'var(--on-surface)' }}>
            <div className="scroll-region" ref={scrollRef}>
              <Variant {...dashProps} />
            </div>
            <AddFlow open={open} setOpen={setOpen} onAdd={addTxn} seedHue={t.seed} dark={t.dark} rm={rm} />
            <SettingsScreen
              open={screen === 'settings'} onClose={() => setScreen(null)} rm={rm}
              variant={t.variant} onVariant={(v) => setTweak('variant', v)}
              dark={t.dark} onToggleTheme={() => setTweak('dark', !t.dark)}
              seedHue={t.seed} onSeedChange={(h) => setTweak('seed', h)} />
            <CycleScreen
              open={screen === 'cycle'} onClose={() => setScreen(null)} rm={rm}
              metrics={metrics} income={data.income} anchorDay={data.anchorDay}
              seedHue={t.seed} dark={t.dark} archive={ARCHIVE} />
          </div>
        </IOSDevice>
      </Stage>

      <TweaksPanel>
        <TweakSection label="Layout" />
        <TweakRadio label="Dashboard" value={t.variant}
          options={VARIANTS.map((v) => ({ value: v.value, label: v.label }))}
          onChange={(v) => setTweak('variant', v)} />
        <TweakToggle label="Dark theme" value={t.dark} onChange={(v) => setTweak('dark', v)} />

        <TweakSection label="Seed colour" />
        <div className="seed-row">
          {SEEDS.map((s) => (
            <button key={s.hue} className="seed-sw" data-on={t.seed === s.hue ? '' : undefined}
              title={s.name} style={{ background: `oklch(0.62 0.14 ${s.hue})` }}
              onClick={() => setTweak('seed', s.hue)} />
          ))}
        </div>

        <TweakSection label="Scenario" />
        <TweakSelect label="Cycle state" value={t.spending}
          options={[
            { value: 'healthy', label: 'Mid-cycle · healthy' },
            { value: 'tight', label: 'Mid-cycle · tight' },
            { value: 'surplus', label: 'Closed · surplus' },
            { value: 'borrowed', label: 'Closed · borrowed' },
          ]}
          onChange={(v) => setTweak('spending', v)} />
        <TweakRadio label="Motion" value={t.motion}
          options={[{ value: 'full', label: 'Full spring' }, { value: 'calm', label: 'Calm' }]}
          onChange={(v) => setTweak('motion', v)} />
      </TweaksPanel>
    </div>
  );
}

ReactDOM.createRoot(document.getElementById('root')).render(<App />);
