function evalNumber(block, env) {
  if (!block) return 0;
  switch (block.type) {
    case 'math_number':      return Number(block.getFieldValue('NUM') || 0);
    case 'variables_get':    return Number(env[block.getFieldValue('VAR')] ?? 0);
    case 'math_random_int': {
      const lo = Math.floor(Math.min(evalNumber(block.getInputTargetBlock('FROM'), env),
                                     evalNumber(block.getInputTargetBlock('TO'), env)));
      const hi = Math.floor(Math.max(evalNumber(block.getInputTargetBlock('FROM'), env),
                                     evalNumber(block.getInputTargetBlock('TO'), env)));
      return lo + Math.floor(Math.random() * (hi - lo + 1));
    }
    case 'math_arithmetic': {
      const A = evalNumber(block.getInputTargetBlock('A'), env);
      const B = evalNumber(block.getInputTargetBlock('B'), env);
      switch (block.getFieldValue('OP')) {
        case 'ADD': return A + B;
        case 'MINUS': return A - B;
        case 'MULTIPLY': return A * B;
        case 'DIVIDE': return B === 0 ? 0 : A / B;
        case 'POWER': return Math.pow(A, B);
      } return 0;
    }
    default: {
      // Fallback: try to read a numeric literal from generated JS
      const code = Blockly.JavaScript.blockToCode(block);
      const n = Number(String(code).match(/-?\d+(?:\.\d+)?/)?.[0] ?? '0');
      return Number.isFinite(n) ? n : 0;
    }
  }
}

function evalBool(block, env) {
  if (!block) return false;
  switch (block.type) {
    case 'logic_boolean':    return block.getFieldValue('BOOL') === 'TRUE';
    case 'logic_negate':     return !evalBool(block.getInputTargetBlock('BOOL'), env);
    case 'logic_operation': {
      const A = evalBool(block.getInputTargetBlock('A'), env);
      const B = evalBool(block.getInputTargetBlock('B'), env);
      return block.getFieldValue('OP') === 'AND' ? (A && B) : (A || B);
    }
    case 'logic_compare': {
      const A = evalNumber(block.getInputTargetBlock('A'), env);
      const B = evalNumber(block.getInputTargetBlock('B'), env);
      switch (block.getFieldValue('OP')) {
        case 'EQ': return A === B; case 'NEQ': return A !== B;
        case 'LT': return A <  B;  case 'LTE': return A <= B;
        case 'GT': return A >  B;  case 'GTE': return A >= B;
      } return false;
    }
    default: return !!evalNumber(block, env);
  }
}

function cmd(name) { return { command: name }; }

function compileStmt(block, env) {
  switch (block.type) {
    // movement
    case 'forward':   return [cmd('forward')];
    case 'back':      return [cmd('back')];
    case 'left':      return [cmd('left')];
    case 'right':     return [cmd('right')];
    case 'up':        return [cmd('up')];
    case 'down':      return [cmd('down')];
    case 'turnleft':  return [cmd('turnleft')];
    case 'turnright': return [cmd('turnright')];

    // place
    case 'setBlock': {
      const alias = block.getFieldValue('BLOCK_ALIAS') || 'DIRT';
      const id = BLOCK_ALIASES[alias] || alias;
      return [{ command: 'setBlock', blockType: id }];
    }

    // variables
    case 'variables_set': {
      const name = block.getFieldValue('VAR');
      env[name] = evalNumber(block.getInputTargetBlock('VALUE'), env);
      return [];
    }

    // repeat N
    case 'repeat': {
      const N = Math.max(0, Math.floor(evalNumber(block.getInputTargetBlock('COUNT'), env)));
      const bodyOnce = [];
      for (let b = block.getInputTargetBlock('DO'); b; b = b.getNextBlock()) {
        bodyOnce.push(...compileStmt(b, env));
      }
      const out = [];
      for (let i = 0; i < N; i++) out.push(...bodyOnce.map(x => ({...x})));
      return out;
    }

    // if / elseif / else (compile-time branching)
    case 'controls_if': {
      let i = 0;
      while (block.getInput('IF' + i)) {
        if (evalBool(block.getInputTargetBlock('IF' + i), env)) {
          const out = [];
          for (let b = block.getInputTargetBlock('DO' + i); b; b = b.getNextBlock()) {
            out.push(...compileStmt(b, env));
          }
          return out;
        }
        i++;
      }
      if (block.getInput('ELSE')) {
        const out = [];
        for (let b = block.getInputTargetBlock('ELSE'); b; b = b.getNextBlock()) {
          out.push(...compileStmt(b, env));
        }
        return out;
      }
      return [];
    }

    default: return [];
  }
}

function compileChain(firstBlock, env) {
  const out = [];
  for (let b = firstBlock; b; b = b.getNextBlock()) {
    out.push(...compileStmt(b, env));
  }
  return out;
}
