// ─────────────────────────────────────────────────────────────
// Add-transaction flow — the FAB morphs into the entry sheet.
// Two modes share one sheet:
//   · Spend     → Essentials / Lifestyle   (draws down spendable)
//   · Set aside → Savings / Investment / Security (kept from income)
// ─────────────────────────────────────────────────────────────

const ENTRY_NOTES = {
  essentials: ['Groceries', 'Rent', 'Transport', 'Utilities'],
  lifestyle:  ['Coffee', 'Dinner out', 'Books', 'Streaming'],
  savings:    ['Emergency fund', 'Holiday pot', 'Rainy day'],
  investment: ['Index fund', 'Pension top-up', 'Shares'],
  security:   ['Insurance', 'Deposit', 'Warranty'],
};

const MODE_CATS = { spend: EXPENSE_CATS, aside: ASIDE_CATS };

function Keypad({ onKey }) {
  const keys = ['1', '2', '3', '4', '5', '6', '7', '8', '9', '.', '0', 'del'];
  return (
    <div className="keypad">
      {keys.map((k) => (
        <button key={k} className="kp-key" data-action={k === 'del' ? '' : undefined}
          onClick={() => { onKey(k); haptic('light'); }}>
          {k === 'del' ? Icon.del : k}
        </button>
      ))}
    </div>
  );
}

function AddFlow({ open, setOpen, onAdd, seedHue, dark, rm }) {
  const [amount, setAmount] = useState('0');
  const [mode, setMode] = useState('spend');     // 'spend' | 'aside'
  const [category, setCategory] = useState('essentials');
  const [customCat, setCustomCat] = useState('');  // freeform category
  const [note, setNote] = useState('');

  // the active category: custom text wins if non-empty
  const activeCat = customCat.trim() || category;
  const isCustom = customCat.trim().length > 0;

  // reset whenever the sheet opens
  useEffect(() => {
    if (open) { setAmount('0'); setMode('spend'); setCategory('essentials'); setCustomCat(''); setNote(''); }
  }, [open]);

  // keep category valid for the active mode
  const switchMode = (m) => {
    setMode(m);
    setCategory(MODE_CATS[m][0]);
    setCustomCat('');
    haptic('light');
  };

  const selectChip = (c) => {
    setCategory(c);
    setCustomCat('');  // clear custom when a chip is tapped
    haptic('light');
  };

  const press = useCallback((k) => {
    setAmount((a) => {
      if (k === 'del') return a.length <= 1 ? '0' : a.slice(0, -1);
      if (k === '.') return a.includes('.') ? a : a + '.';
      if (a.includes('.') && a.split('.')[1].length >= 2) return a;     // max 2dp
      if (a === '0' && k !== '.') return k;
      if (a.replace('.', '').length >= 7) return a;                     // sane cap
      return a + k;
    });
  }, []);

  const value = parseFloat(amount) || 0;
  const accent = catColor(activeCat, seedHue, dark);
  const valid = value > 0;

  const commit = () => {
    if (!valid) return;
    haptic('heavy');
    const cat = activeCat.toLowerCase().trim();
    const kind = mode === 'aside' ? 'aside' : 'expense';
    const fallbackNote = ENTRY_NOTES[cat] ? ENTRY_NOTES[cat][0] : cat;
    onAdd({ amount: value, category: cat, kind, note: note.trim() || fallbackNote });
    setOpen(false);
  };

  const sheetStyle = open
    ? { height: 'calc(100% - 64px)', bottom: 0, borderRadius: '30px 30px 0 0',
        width: '100%', background: 'var(--sc-high)' }
    : { height: 64, bottom: 40, borderRadius: 40, width: 64, background: 'var(--primary)' };

  const cats = MODE_CATS[mode];

  return (
    <>
      <div className="scrim" data-on={open ? '' : undefined} onClick={() => setOpen(false)} />

      <div className="morph" data-open={open ? '' : undefined} data-rm={rm ? '' : undefined}
        style={{ ...sheetStyle, '--bk': accent }}
        onClick={!open ? () => { haptic('medium'); setOpen(true); } : undefined}
        role={!open ? 'button' : undefined} aria-label={!open ? 'Add transaction' : undefined}>

        {/* FAB face */}
        <div className="fab-face" data-hide={open ? '' : undefined} style={{ color: 'var(--on-primary)' }}>
          {Icon.plus}
        </div>

        {/* Sheet face */}
        <div className="sheet-face" data-show={open ? '' : undefined}>
          <div className="sheet-grip" />
          <div className="sheet-head">
            <span className="t-title-l emph" style={{ color: 'var(--on-surface)' }}>New entry</span>
            <IconButton label="Close" onClick={() => setOpen(false)}>{Icon.back}</IconButton>
          </div>

          {/* mode toggle: Spend vs Set aside */}
          <div className="mode-seg">
            <button className="mode-btn" data-on={mode === 'spend' ? '' : undefined}
              onClick={() => switchMode('spend')}>Spend</button>
            <button className="mode-btn" data-on={mode === 'aside' ? '' : undefined}
              onClick={() => switchMode('aside')}>Set aside</button>
          </div>

          <div className="amount-display">
            <span className="amt" style={{ color: valid ? 'var(--bk)' : 'var(--on-variant)' }}>
              <span className="amt-cur">£</span>{amount}
            </span>
          </div>

          <input className="note-input" placeholder={`Add a note · e.g. ${ENTRY_NOTES[activeCat] ? ENTRY_NOTES[activeCat][0] : 'Groceries'}`}
            value={note} maxLength={32} onChange={(e) => setNote(e.target.value)} />

          <div className="bucket-row" data-count={cats.length}>
            {cats.map((c) => {
              const col = catColor(c, seedHue, dark);
              const on = !isCustom && category === c;
              return (
                <button key={c} className="bucket-btn" data-on={on ? '' : undefined}
                  style={{ '--c': col, '--soft': catSoft(c, seedHue, dark) }}
                  onClick={() => selectChip(c)}>
                  {catLabel(c)}
                </button>
              );
            })}
          </div>

          <input className="custom-cat-input" placeholder="or type a category…"
            value={customCat} maxLength={24}
            onChange={(e) => setCustomCat(e.target.value)}
            style={isCustom ? { borderColor: accent, '--dot': accent } : undefined} />

          <Keypad onKey={press} />

          <button className="commit-btn" data-valid={valid ? '' : undefined}
            style={{ background: valid ? 'var(--bk)' : 'var(--sc-highest)', color: valid ? 'var(--on-primary)' : 'var(--on-variant)' }}
            onClick={commit}>
            {Icon.check}<span>{mode === 'aside' ? 'Set aside' : 'Add'} {catLabel(activeCat).toLowerCase()}</span>
          </button>
        </div>
      </div>
    </>
  );
}

Object.assign(window, { AddFlow });
