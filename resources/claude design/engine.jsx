// ─────────────────────────────────────────────────────────────
// Phase 0 — Salary-cycle model + deterministic metric engine
// Pure, side-effect-free. Given the same inputs → the same outputs.
// ─────────────────────────────────────────────────────────────

// days in a given month (month is 0-indexed)
function daysInMonth(year, month) {
  return new Date(year, month + 1, 0).getDate();
}
// clamp an anchor day to a month that may be shorter (e.g. 31 → 28/29/30)
function clampDay(year, month, day) {
  return Math.min(day, daysInMonth(year, month));
}
// midnight of a date — strips wall-clock time so math is deterministic
function dateOnly(d) {
  return new Date(d.getFullYear(), d.getMonth(), d.getDate());
}
const MS_DAY = 86400000;

// Derive the active salary cycle for `today`, anchored to `anchorDay`.
// Returns start, end (inclusive), totalDays, dayIndex (0-based elapsed),
// remaining (>=1), and progress (0..1).
function cycleFor(today, anchorDay) {
  const t = dateOnly(today);
  const y = t.getFullYear(), m = t.getMonth(), d = t.getDate();

  // Has this month's anchor already passed?
  let startY = y, startM = m;
  if (d < clampDay(y, m, anchorDay)) {
    startM = m - 1;
    if (startM < 0) { startM = 11; startY = y - 1; }
  }
  const start = new Date(startY, startM, clampDay(startY, startM, anchorDay));

  // next anchor → end is the day before it
  let endM = startM + 1, endY = startY;
  if (endM > 11) { endM = 0; endY = startY + 1; }
  const nextAnchor = new Date(endY, endM, clampDay(endY, endM, anchorDay));
  const end = new Date(nextAnchor.getTime() - MS_DAY);

  const totalDays = Math.round((nextAnchor - start) / MS_DAY);
  const dayIndex = Math.round((t - start) / MS_DAY);
  const remaining = Math.max(1, totalDays - dayIndex);
  const progress = Math.min(1, Math.max(0, dayIndex / totalDays));

  return { start, end, totalDays, dayIndex, remaining, progress };
}

function inCycle(isoDate, cyc) {
  const d = dateOnly(new Date(isoDate + 'T00:00:00'));
  return d >= cyc.start && d <= cyc.end;
}

// Compute every figure the interface displays — the salaried waterfall.
//   income − asides(savings+investment+security) = spendable
//   spendable − expenses(essentials+lifestyle)   = balance
//   balance ÷ days-remaining                      = safe-to-spend / day
function computeMetrics(state, today) {
  const cyc = cycleFor(today, state.anchorDay);
  const txns = state.transactions
    .filter((tx) => inCycle(tx.date, cyc))
    .sort((a, b) => (a.date < b.date ? 1 : a.date > b.date ? -1 : b.id - a.id));

  const byCategory = {};
  txns.forEach((tx) => { byCategory[tx.category] = (byCategory[tx.category] || 0) + tx.amount; });

  // sum by kind — works for both known and custom categories
  let allocated = 0, spent = 0;
  Object.entries(byCategory).forEach(([cat, amt]) => {
    const kind = catKind(cat, txns.find(tx => tx.category === cat)?.kind);
    if (kind === 'aside') allocated += amt; else spent += amt;
  });
  const spendable = state.income - allocated;     // money free to spend this cycle
  const balance = spendable - spent;              // what remains right now
  const safeToSpend = balance / cyc.remaining;

  // even-burn baseline of the spendable pot
  const baseline = spendable / cyc.totalDays;
  const elapsed = Math.max(1, cyc.dayIndex + 1);
  const dailyVelocity = spent / elapsed;          // expense pace so far
  const projectedSpend = dailyVelocity * cyc.totalDays;
  const projectedClose = spendable - projectedSpend; // +surplus / −borrowed at pace

  // tight = remaining daily allowance well under an even burn
  const tight = safeToSpend < baseline * 0.6;

  // cycle close (actual): leftover is surplus, overspend is borrowed
  const ended = state.ended === true || today > cyc.end;
  const surplus = Math.max(0, balance);
  const borrowed = Math.max(0, -balance);

  return {
    cyc, txns, byCategory,
    income: state.income, allocated, spent, spendable, balance, safeToSpend,
    baseline, dailyVelocity, projectedSpend, projectedClose,
    tight, ended, surplus, borrowed,
  };
}

// local ISO key (yyyy-mm-dd) — avoids the UTC shift of toISOString()
function isoLocal(d) {
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
}

// group transactions by ISO day, newest first, with a friendly label
function groupByDay(txns, today) {
  const tISO = isoLocal(dateOnly(today));
  const yISO = isoLocal(new Date(dateOnly(today).getTime() - MS_DAY));
  const map = new Map();
  txns.forEach((tx) => {
    if (!map.has(tx.date)) map.set(tx.date, []);
    map.get(tx.date).push(tx);
  });
  return [...map.entries()].map(([date, items]) => {
    const d = new Date(date + 'T00:00:00');
    let label;
    if (date === tISO) label = 'TODAY';
    else if (date === yISO) label = 'YESTERDAY';
    else label = d.toLocaleDateString('en-GB', { weekday: 'short', day: 'numeric', month: 'short' }).toUpperCase();
    const total = items.reduce((s, x) => s + x.amount, 0);
    return { date, label, items, total };
  });
}

Object.assign(window, {
  daysInMonth, clampDay, dateOnly, cycleFor, computeMetrics, groupByDay,
});
