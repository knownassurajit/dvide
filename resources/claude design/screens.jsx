// ─────────────────────────────────────────────────────────────
// Push-navigation screens that slide in over the dashboard.
//   · SettingsScreen — display style, account, region
//   · CycleScreen    — this cycle's config, totals, past-cycle archive
// Both share <ScreenShell> (slide-in panel + header).
// ─────────────────────────────────────────────────────────────

function ScreenShell({ open, title, onClose, rm, children }) {
  return (
    <div className={'screen' + (rm ? ' screen-rm' : '')} data-open={open ? '' : undefined} aria-hidden={!open}>
      <div className="screen-head">
        <IconButton label="Back" onClick={onClose}>{Icon.back}</IconButton>
        <span className="t-headline-s emph" style={{ color: 'var(--on-surface)' }}>{title}</span>
      </div>
      <div className="screen-body">{children}</div>
    </div>
  );
}

// a labelled section of grouped rows
function Group({ label, children }) {
  return (
    <>
      {label && <div className="set-label">{label}</div>}
      <div className="set-group">{children}</div>
    </>
  );
}

// a single list row: leading label, trailing value/control, optional chevron
function Row({ label, sub, value, onClick, chevron }) {
  return (
    <div className="set-row" data-tappable={onClick ? '' : undefined} onClick={onClick}>
      <div style={{ flex: 1, minWidth: 0 }}>
        <div className="t-body-l" style={{ color: 'var(--on-surface)' }}>{label}</div>
        {sub && <div className="t-label-s" style={{ color: 'var(--on-variant)', marginTop: 2 }}>{sub}</div>}
      </div>
      {value !== undefined && <span className="t-body-l" style={{ color: 'var(--on-variant)', whiteSpace: 'nowrap' }}>{value}</span>}
      {chevron && <span style={{ color: 'var(--on-variant)', display: 'flex', opacity: .7 }}>{Icon.chev}</span>}
    </div>
  );
}

// ── full-spectrum hue slider ──
function HueSlider({ value, onChange }) {
  const trackRef = useRef(null);
  const dragging = useRef(false);

  const hueFromEvent = useCallback((e) => {
    const rect = trackRef.current.getBoundingClientRect();
    const clientX = e.touches ? e.touches[0].clientX : e.clientX;
    const pct = Math.max(0, Math.min(1, (clientX - rect.left) / rect.width));
    return Math.round(pct * 360);
  }, []);

  const onStart = useCallback((e) => {
    e.preventDefault();
    dragging.current = true;
    onChange(hueFromEvent(e));
    const move = (ev) => { if (dragging.current) onChange(hueFromEvent(ev)); };
    const up = () => { dragging.current = false; document.removeEventListener('pointermove', move); document.removeEventListener('pointerup', up); };
    document.addEventListener('pointermove', move);
    document.addEventListener('pointerup', up);
  }, [onChange, hueFromEvent]);

  const pct = ((value % 360) / 360 * 100).toFixed(1);
  const thumbColor = `oklch(0.65 0.15 ${value})`;

  return (
    <div className="hue-slider" onPointerDown={onStart} ref={trackRef}>
      <div className="hue-track"></div>
      <div className="hue-thumb" style={{ left: `${pct}%`, background: thumbColor }}></div>
    </div>
  );
}

// ════════════════════════════ SETTINGS ════════════════════════════
function SettingsScreen({ open, onClose, rm, variant, onVariant, dark, onToggleTheme, seedHue, onSeedChange }) {
  const VARIANTS = [
    { value: 'editorial', label: 'Editorial' },
    { value: 'gauge', label: 'Gauge' },
    { value: 'cards', label: 'Cards' },
  ];
  return (
    <ScreenShell open={open} title="Settings" onClose={onClose} rm={rm}>
      {/* account identity */}
      <div className="set-account">
        <div className="set-avatar">S</div>
        <div>
          <div className="t-title-l emph" style={{ color: 'var(--on-surface)' }}>Sam Whitfield</div>
          <div className="t-label-m" style={{ color: 'var(--on-variant)' }}>sam@whitfield.me</div>
        </div>
      </div>

      <Group label="Display">
        <div className="set-row" style={{ flexDirection: 'column', alignItems: 'stretch', gap: 12 }}>
          <div className="t-body-l" style={{ color: 'var(--on-surface)' }}>Dashboard style</div>
          <Segmented value={variant} onChange={onVariant} options={VARIANTS} />
        </div>
        <div className="set-row">
          <div style={{ flex: 1 }}>
            <div className="t-body-l" style={{ color: 'var(--on-surface)' }}>Dark theme</div>
          </div>
          <button className="m3-switch" data-on={dark ? '' : undefined} onClick={onToggleTheme} aria-label="Dark theme">
            <span className="m3-switch-knob" />
          </button>
        </div>
        <div className="set-row" style={{ flexDirection: 'column', alignItems: 'stretch', gap: 12 }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
            <div className="t-body-l" style={{ color: 'var(--on-surface)', flex: 1 }}>App colour</div>
            <span className="hue-preview" style={{ background: `oklch(0.65 0.15 ${seedHue})` }}></span>
          </div>
          <HueSlider value={seedHue} onChange={onSeedChange} />
        </div>
      </Group>

      <Group label="Region & currency">
        <Row label="Currency" value="GBP · £" chevron />
        <Row label="Region" value="United Kingdom" chevron />
        <Row label="Week starts on" value="Monday" chevron />
        <Row label="Number format" value="1,234.56" chevron />
      </Group>

      <Group label="Account">
        <Row label="Personal details" sub="Name, email, photo" chevron onClick={() => {}} />
        <Row label="Manual-only mode" sub="No bank feeds — every entry is yours" value="On" />
        <Row label="Export ledger" sub="Download a CSV of this cycle" chevron onClick={() => {}} />
      </Group>

      <div className="t-label-s" style={{ color: 'var(--on-variant)', textAlign: 'center', padding: '4px 0 8px', letterSpacing: '.06em' }}>
        Cyclewise · v0.3 · made by hand
      </div>
    </ScreenShell>
  );
}

// ════════════════════════════ CYCLE DETAIL ════════════════════════════
function CycleScreen({ open, onClose, rm, metrics, income, anchorDay, seedHue, dark, archive }) {
  const c = metrics.cyc;
  const ord = (n) => n + (['th', 'st', 'nd', 'rd'][(n % 100 - 20) % 10] || ['th', 'st', 'nd', 'rd'][n % 100] || 'th');
  const close = (bal) => {
    const pos = bal >= 0;
    return (
      <span className="t-body-l emph" style={{ color: pos ? 'var(--on-surface)' : 'var(--status)', whiteSpace: 'nowrap' }}>
        {pos ? '+' : '−'}{money(bal)}
      </span>
    );
  };
  const ended = metrics.ended;
  const surplus = metrics.balance >= 0;

  // a labelled waterfall row with an arithmetic operator
  const wfRow = (label, value, { op = '', strong = false, rule = false, sub = null, tone = null } = {}) => (
    <div className={'set-row' + (rule ? ' wf-divider' : '')}>
      <div style={{ flex: 1, minWidth: 0 }}>
        <div className={'t-body-l' + (strong ? ' emph' : '')} style={{ color: tone || (op === '−' ? 'var(--on-variant)' : 'var(--on-surface)') }}>
          {op && <span style={{ display: 'inline-block', width: 16, color: 'var(--on-variant)' }}>{op}</span>}{label}
        </div>
        {sub && <div className="t-label-s" style={{ color: 'var(--on-variant)', marginTop: 2, marginLeft: op ? 16 : 0 }}>{sub}</div>}
      </div>
      <span className={'t-body-l' + (strong ? ' emph' : '')} style={{ color: tone || (op === '−' ? 'var(--on-variant)' : 'var(--on-surface)'), whiteSpace: 'nowrap' }}>
        {op === '−' ? '− ' : ''}{money(value)}
      </span>
    </div>
  );

  const catRow = (cat) => {
    const v = metrics.byCategory[cat];
    if (v <= 0) return null;
    const denom = catKind(cat) === 'aside' ? metrics.allocated : metrics.spent;
    const pct = denom > 0 ? v / denom * 100 : 0;
    return (
      <div key={cat} className="cyc-bucket">
        <div style={{ display: 'flex', alignItems: 'center', gap: 9 }}>
          <span style={{ width: 9, height: 9, borderRadius: 99, background: catColor(cat, seedHue, dark) }} />
          <span className="t-body-l" style={{ color: 'var(--on-surface)', flex: 1 }}>{catLabel(cat)}</span>
          <span className="t-body-l emph" style={{ color: 'var(--on-surface)' }}>{money(v)}</span>
        </div>
        <div className="bk-track" style={{ marginTop: 9 }}>
          <div className="bk-fill" style={{ width: Math.max(2, pct) + '%', background: catColor(cat, seedHue, dark) }} />
        </div>
      </div>
    );
  };

  return (
    <ScreenShell open={open} title={ended ? 'Cycle closed' : 'This cycle'} onClose={onClose} rm={rm}>
      {/* hero totals — outcome when closed, runway when live */}
      <div className={'cyc-summary' + (ended ? ' cyc-closed' : '')} data-pos={ended && surplus ? '' : undefined}>
        <div className="t-label-m" style={{ color: 'var(--on-variant)', letterSpacing: '.16em' }}>{cycleLabel(c)}</div>
        <div className="ch-num" style={{ color: ended && !surplus ? 'var(--status)' : 'var(--on-surface)', fontSize: 46, margin: '4px 0 2px' }}>
          {ended && (surplus ? '+' : '−')}{money(Math.abs(metrics.balance))}
          <span style={{ fontSize: '.4em', fontWeight: 600, color: 'var(--on-variant)' }}> {ended ? (surplus ? 'surplus' : 'borrowed') : 'left to spend'}</span>
        </div>
        <div className="t-label-m" style={{ color: 'var(--on-variant)', letterSpacing: '.06em' }}>
          {ended ? `${c.totalDays} days · settled` : `${c.dayIndex + 1} of ${c.totalDays} days · ${c.remaining} remaining`}
        </div>
      </div>

      <Group label="Configuration">
        <Row label="Cycle anchor" sub="Salary lands this day each month" value={ord(anchorDay)} chevron onClick={() => {}} />
        <Row label="Monthly income" value={money(income)} chevron onClick={() => {}} />
        <Row label="Cycle window" value={cycleLabel(c)} />
      </Group>

      {/* the waterfall */}
      <Group label="The waterfall">
        {wfRow('Income', metrics.income)}
        {wfRow('Set aside', metrics.allocated, { op: '−', sub: 'Savings · Investment · Security' })}
        {wfRow('Spendable', metrics.spendable, { op: '=', strong: true, rule: true })}
        {wfRow('Spent', metrics.spent, { op: '−', sub: 'Essentials · Lifestyle' })}
        {wfRow(ended ? (surplus ? 'Surplus' : 'Borrowed') : 'Balance', Math.abs(metrics.balance),
          { op: '=', strong: true, rule: true, tone: surplus ? 'var(--on-surface)' : 'var(--status)' })}
      </Group>

      {!ended &&
      <Group label="Outlook">
        <div className="set-row" style={{ background: 'var(--sc-low)' }}>
          <div style={{ flex: 1 }}>
            <div className="t-body-l" style={{ color: 'var(--on-surface)' }}>Projected close</div>
            <div className="t-label-s" style={{ color: 'var(--on-variant)', marginTop: 2 }}>At your current spending pace</div>
          </div>
          {close(metrics.projectedClose)}
        </div>
        <Row label="Safe to spend" sub={`Per day · for ${c.remaining} days`} value={money(metrics.safeToSpend, 2)} />
      </Group>
      }

      {/* set-aside breakdown — known + any custom aside categories */}
      <div className="set-label">Set aside · {money(metrics.allocated)}</div>
      <div className="set-group" style={{ padding: '6px 0' }}>
        {Object.keys(metrics.byCategory)
          .filter((c) => catKind(c, metrics.txns.find(tx => tx.category === c)?.kind) === 'aside' && metrics.byCategory[c] > 0)
          .map(catRow)}
      </div>

      {/* spending breakdown — known + any custom expense categories */}
      <div className="set-label">Spent · {money(metrics.spent)}</div>
      <div className="set-group" style={{ padding: '6px 0' }}>
        {Object.keys(metrics.byCategory)
          .filter((c) => catKind(c, metrics.txns.find(tx => tx.category === c)?.kind) === 'expense' && metrics.byCategory[c] > 0)
          .map(catRow)}
      </div>

      {/* archive of closed cycles */}
      <div className="set-label">Past cycles</div>
      <div className="set-group">
        {archive.map((a) => (
          <div key={a.label} className="set-row" data-tappable="" onClick={() => {}}>
            <div style={{ flex: 1, minWidth: 0 }}>
              <div className="t-body-l" style={{ color: 'var(--on-surface)' }}>{a.label}</div>
              <div className="t-label-s" style={{ color: 'var(--on-variant)', marginTop: 2 }}>{a.range}</div>
            </div>
            {close(a.balance)}
            <span style={{ color: 'var(--on-variant)', display: 'flex', opacity: .6, marginLeft: 4 }}>{Icon.chev}</span>
          </div>
        ))}
      </div>
    </ScreenShell>
  );
}

Object.assign(window, { SettingsScreen, CycleScreen });
