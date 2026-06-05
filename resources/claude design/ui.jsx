// ─────────────────────────────────────────────────────────────
// Shared UI primitives + hooks (used by all dashboard variants)
// ─────────────────────────────────────────────────────────────
const { useState, useEffect, useRef, useCallback } = React;

function useReducedMotion() {
  const [rm, setRm] = useState(
    () => window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches
  );
  useEffect(() => {
    const mq = window.matchMedia('(prefers-reduced-motion: reduce)');
    const fn = () => setRm(mq.matches);
    mq.addEventListener && mq.addEventListener('change', fn);
    return () => mq.removeEventListener && mq.removeEventListener('change', fn);
  }, []);
  return rm;
}

// crisp/heavy taps — harmless where unsupported
function haptic(kind) {
  if (!navigator.vibrate) return;
  navigator.vibrate(kind === 'heavy' ? [16, 24, 12] : kind === 'medium' ? 12 : 7);
}

// Velocity-driven kinetic list: rows compress/flex as the user scrolls.
// Returns a ref for the scroller; applies --kv (kinetic velocity) on rows.
function useKineticScroll(enabled) {
  const ref = useRef(null);
  useEffect(() => {
    const el = ref.current;
    if (!el || !enabled) return;
    let last = el.scrollTop, lastT = performance.now(), raf = 0, v = 0;
    const decay = () => {
      v *= 0.86;
      el.style.setProperty('--kv', v.toFixed(3));
      if (Math.abs(v) > 0.02) raf = requestAnimationFrame(decay);
      else { v = 0; el.style.setProperty('--kv', '0'); raf = 0; }
    };
    const onScroll = () => {
      const now = performance.now();
      const dt = Math.max(8, now - lastT);
      const dv = (el.scrollTop - last) / dt;        // px per ms
      v = Math.max(-1, Math.min(1, dv * 0.9));
      last = el.scrollTop; lastT = now;
      if (!raf) raf = requestAnimationFrame(decay);
    };
    el.addEventListener('scroll', onScroll, { passive: true });
    return () => { el.removeEventListener('scroll', onScroll); cancelAnimationFrame(raf); };
  }, [enabled]);
  return ref;
}

// Segmented control (daily / weekly, etc.)
function Segmented({ options, value, onChange, small }) {
  return (
    <div className="m3-seg" data-small={small ? '' : undefined} role="tablist">
      {options.map((o) => (
        <button
          key={o.value}
          role="tab"
          aria-selected={value === o.value}
          className="m3-seg-btn"
          data-on={value === o.value ? '' : undefined}
          onClick={() => { onChange(o.value); haptic('light'); }}
        >
          {o.label}
        </button>
      ))}
    </div>
  );
}

// small round icon button
function IconButton({ children, onClick, label }) {
  return (
    <button className="m3-iconbtn" aria-label={label} onClick={onClick}>{children}</button>
  );
}

// ── Tiny inline glyphs (only simple primitives — no illustration) ──
const Icon = {
  sun: (
    <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round">
      <circle cx="12" cy="12" r="4.2" /><path d="M12 2.5v2M12 19.5v2M2.5 12h2M19.5 12h2M5 5l1.4 1.4M17.6 17.6 19 19M19 5l-1.4 1.4M6.4 17.6 5 19" />
    </svg>
  ),
  moon: (
    <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M20 14.5A8 8 0 0 1 9.5 4a7 7 0 1 0 10.5 10.5Z" />
    </svg>
  ),
  plus: (
    <svg viewBox="0 0 24 24" width="28" height="28" fill="none" stroke="currentColor" strokeWidth="2.6" strokeLinecap="round">
      <path d="M12 5v14M5 12h14" />
    </svg>
  ),
  back: (
    <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" strokeWidth="2.4" strokeLinecap="round" strokeLinejoin="round">
      <path d="M15 5l-7 7 7 7" />
    </svg>
  ),
  del: (
    <svg viewBox="0 0 24 24" width="26" height="26" fill="none" stroke="currentColor" strokeWidth="2" strokeLinejoin="round" strokeLinecap="round">
      <path d="M9 6h10a2 2 0 0 1 2 2v8a2 2 0 0 1-2 2H9l-6-6 6-6Z" /><path d="M16 9.5l-5 5M11 9.5l5 5" />
    </svg>
  ),
  check: (
    <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" strokeWidth="2.8" strokeLinecap="round" strokeLinejoin="round">
      <path d="M4 12.5l5 5L20 6.5" />
    </svg>
  ),
  gear: (
    <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
      <path d="M19.14 12.94c.04-.3.06-.61.06-.94 0-.32-.02-.64-.07-.94l2.03-1.58a.49.49 0 0 0 .12-.61l-1.92-3.32a.49.49 0 0 0-.59-.22l-2.39.96a7.03 7.03 0 0 0-1.62-.94l-.36-2.54a.48.48 0 0 0-.48-.41h-3.84a.48.48 0 0 0-.48.41l-.36 2.54c-.59.24-1.13.56-1.62.94l-2.39-.96a.48.48 0 0 0-.59.22L2.74 8.87a.48.48 0 0 0 .12.61l2.03 1.58c-.05.3-.09.63-.09.94s.02.64.07.94l-2.03 1.58a.49.49 0 0 0-.12.61l1.92 3.32c.12.22.39.3.59.22l2.39-.96c.5.38 1.03.7 1.62.94l.36 2.54c.05.24.24.41.48.41h3.84c.24 0 .44-.17.48-.41l.36-2.54c.59-.24 1.13-.56 1.62-.94l2.39.96c.22.08.47 0 .59-.22l1.92-3.32a.49.49 0 0 0-.12-.61l-2.01-1.58zM12 15.6A3.6 3.6 0 1 1 12 8.4a3.6 3.6 0 0 1 0 7.2z" />
    </svg>
  ),
  chev: (
    <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M9 5l7 7-7 7" />
    </svg>
  ),
};

// SVG ring gauge — value 0..1, with an optional secondary track
function RingGauge({ value, size = 200, stroke = 18, color, track, children, sharp }) {
  const r = (size - stroke) / 2;
  const c = 2 * Math.PI * r;
  const start = 0.5; // leave a gap at the bottom (270° sweep)
  const sweep = 0.78;
  const dash = c * sweep;
  const off = dash * (1 - Math.max(0, Math.min(1, value)));
  const rot = 90 + (1 - sweep) * 180; // rotate so gap is centred at bottom
  return (
    <div style={{ position: 'relative', width: size, height: size }}>
      <svg width={size} height={size} style={{ transform: `rotate(${rot}deg)`, transition: 'all .5s cubic-bezier(.34,1.3,.5,1)' }}>
        <circle cx={size / 2} cy={size / 2} r={r} fill="none" stroke={track} strokeWidth={stroke}
          strokeLinecap={sharp ? 'butt' : 'round'} strokeDasharray={`${dash} ${c}`} />
        <circle cx={size / 2} cy={size / 2} r={r} fill="none" stroke={color} strokeWidth={stroke}
          strokeLinecap={sharp ? 'butt' : 'round'} strokeDasharray={`${dash} ${c}`} strokeDashoffset={off}
          style={{ transition: 'stroke-dashoffset .7s cubic-bezier(.34,1.3,.5,1), stroke .4s ease' }} />
      </svg>
      <div style={{ position: 'absolute', inset: 0, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center' }}>
        {children}
      </div>
    </div>
  );
}

Object.assign(window, {
  useReducedMotion, haptic, useKineticScroll, Segmented, IconButton, Icon, RingGauge,
});
