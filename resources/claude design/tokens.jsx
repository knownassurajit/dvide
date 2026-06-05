// ─────────────────────────────────────────────────────────────
// Phase 0 — Token layer
// Tonal colour roles derived from a single seed hue, for light & dark.
// No raw hex downstream — everything resolves through buildRoles().
// ─────────────────────────────────────────────────────────────

const ok = (l, c, h) => `oklch(${l} ${c} ${h})`;

// Surface/role tokens for a given seed hue + theme.
function buildRoles(hue, dark) {
  if (dark) {
    return {
      surface:               ok(0.165, 0.012, hue),
      surfaceDim:            ok(0.145, 0.012, hue),
      surfaceContainerLowest: ok(0.125, 0.012, hue),
      surfaceContainerLow:   ok(0.205, 0.014, hue),
      surfaceContainer:      ok(0.235, 0.016, hue),
      surfaceContainerHigh:  ok(0.275, 0.018, hue),
      surfaceContainerHighest: ok(0.315, 0.020, hue),
      onSurface:             ok(0.945, 0.010, hue),
      onSurfaceVariant:      ok(0.760, 0.022, hue),
      outline:               ok(0.520, 0.020, hue),
      outlineFaint:          ok(0.330, 0.014, hue),
      primary:               ok(0.815, 0.110, hue),
      onPrimary:             ok(0.250, 0.070, hue),
      primaryContainer:      ok(0.400, 0.095, hue),
      onPrimaryContainer:    ok(0.910, 0.060, hue),
      status:                ok(0.745, 0.085, 47),   // oxidised clay / terracotta
      statusContainer:       ok(0.380, 0.060, 45),
      scrim:                 'rgba(0,0,0,0.55)',
    };
  }
  return {
    surface:               ok(0.982, 0.006, hue),
    surfaceDim:            ok(0.935, 0.010, hue),
    surfaceContainerLowest: ok(1.000, 0.000, hue),
    surfaceContainerLow:   ok(0.968, 0.008, hue),
    surfaceContainer:      ok(0.950, 0.010, hue),
    surfaceContainerHigh:  ok(0.928, 0.012, hue),
    surfaceContainerHighest: ok(0.905, 0.014, hue),
    onSurface:             ok(0.205, 0.020, hue),
    onSurfaceVariant:      ok(0.440, 0.026, hue),
    outline:               ok(0.620, 0.022, hue),
    outlineFaint:          ok(0.860, 0.012, hue),
    primary:               ok(0.500, 0.135, hue),
    onPrimary:             ok(0.995, 0.005, hue),
    primaryContainer:      ok(0.900, 0.060, hue),
    onPrimaryContainer:    ok(0.300, 0.110, hue),
    status:                ok(0.560, 0.105, 45),
    statusContainer:       ok(0.905, 0.045, 50),
    scrim:                 'rgba(0,0,0,0.40)',
  };
}

// ─────────────────────────────────────────────────────────────
// Category taxonomy — the real-world salaried model.
// Two KINDS sit on one income waterfall:
//   · aside   — money kept (Savings / Investment / Security)
//   · expense — money spent (Essentials / Lifestyle)
// Both deduct from income; asides are deducted first to reveal
// the spendable amount, expenses draw that spendable down.
// Each category owns a harmonious hue (Essentials = the seed).
// ─────────────────────────────────────────────────────────────
const CATEGORIES = {
  savings:    { label: 'Savings',    kind: 'aside',   hue: 168, icon: 'piggy' },
  investment: { label: 'Investment', kind: 'aside',   hue: 262, icon: 'growth' },
  security:   { label: 'Security',   kind: 'aside',   hue: 28,  icon: 'shield' },
  essentials: { label: 'Essentials', kind: 'expense', hue: null, icon: 'home' },
  lifestyle:  { label: 'Lifestyle',  kind: 'expense', hue: 75,  icon: 'spark' },
};
const ASIDE_CATS   = ['savings', 'investment', 'security'];
const EXPENSE_CATS = ['essentials', 'lifestyle'];
const ALL_CATS     = [...ASIDE_CATS, ...EXPENSE_CATS];

// hash an arbitrary string to a stable hue (0–360) for custom categories
function hashHue(str) {
  let h = 0;
  for (let i = 0; i < str.length; i++) h = str.charCodeAt(i) + ((h << 5) - h);
  return ((h % 360) + 360) % 360;
}

function catHue(cat, seedHue) {
  if (CATEGORIES[cat]) {
    const h = CATEGORIES[cat].hue;
    return h == null ? seedHue : h;
  }
  return hashHue(cat); // custom category → deterministic hue from name
}
function catColor(cat, seedHue, dark) {
  const h = catHue(cat, seedHue);
  return dark ? ok(0.815, 0.105, h) : ok(0.520, 0.130, h);
}
function catSoft(cat, seedHue, dark) {
  const h = catHue(cat, seedHue);
  return dark ? ok(0.380, 0.075, h) : ok(0.905, 0.055, h);
}
// label: known → pretty name; custom → capitalise first letter
const catLabel = (cat) => CATEGORIES[cat] ? CATEGORIES[cat].label : (cat.charAt(0).toUpperCase() + cat.slice(1));
// kind: known → aside/expense; custom → read tx.kind or default expense
const catKind  = (cat, txKind) => CATEGORIES[cat] ? CATEGORIES[cat].kind : (txKind || 'expense');

// currency formatting (£, no decimals for the hero, 2dp elsewhere)
function money(n, dp = 0) {
  return '£' + Math.abs(n).toLocaleString('en-GB', {
    minimumFractionDigits: dp, maximumFractionDigits: dp,
  });
}
function moneyParts(n) {
  const whole = Math.floor(Math.abs(n));
  const frac = Math.round((Math.abs(n) - whole) * 100);
  return {
    whole: whole.toLocaleString('en-GB'),
    frac: String(frac).padStart(2, '0'),
  };
}
function cycleLabel(cyc) {
  const f = (d) => d.toLocaleDateString('en-GB', { day: 'numeric', month: 'short' }).toUpperCase();
  return `${f(cyc.start)} – ${f(cyc.end)}`;
}

Object.assign(window, {
  buildRoles, CATEGORIES, ASIDE_CATS, EXPENSE_CATS, ALL_CATS,
  catHue, catColor, catSoft, catLabel, catKind, hashHue,
  money, moneyParts, cycleLabel,
});
