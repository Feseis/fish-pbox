(() => {
  'use strict';

  const API = '/sms/senders';
  const API_PHONES = '/sms/phones';

  const state = {
    senders: [],
    phones: [],
    editingPhone: null,
  };

  const els = {
    serverUrl: document.getElementById('server-url'),
    form: document.getElementById('register-form'),
    phone: document.getElementById('phone'),
    senderName: document.getElementById('sender-name'),
    registerBtn: document.getElementById('register-btn'),
    refreshBtn: document.getElementById('refresh-btn'),
    body: document.getElementById('senders-body'),
    count: document.getElementById('sender-count'),
    dialog: document.getElementById('rename-dialog'),
    renameForm: document.getElementById('rename-form'),
    renamePhone: document.getElementById('rename-phone'),
    renameName: document.getElementById('rename-name'),
    renameCancel: document.getElementById('rename-cancel'),
    renameSave: document.getElementById('rename-save'),
    toastRoot: document.getElementById('toast-root'),
    phoneForm: document.getElementById('phone-form'),
    phoneInput: document.getElementById('app-phone'),
    phoneRegisterBtn: document.getElementById('phone-register-btn'),
    phoneRefreshBtn: document.getElementById('phone-refresh-btn'),
    phonesBody: document.getElementById('phones-body'),
    phoneCount: document.getElementById('phone-count'),
  };

  function maskCode(code) {
    if (!code) {
      return '—';
    }
    if (code.length <= 12) {
      return code;
    }
    return `${code.slice(0, 6)}••••••${code.slice(-4)}`;
  }

  async function request(url, options = {}) {
    const response = await fetch(url, options);
    const raw = await response.text();
    let data = null;
    if (raw) {
      try {
        data = JSON.parse(raw);
      } catch (error) {
        data = null;
      }
    }
    if (!response.ok) {
      const detail = (data && (data.detail || data.message)) || `请求失败（HTTP ${response.status}）`;
      const code = data && data.code ? `${data.code}: ` : '';
      throw new Error(code + detail);
    }
    return data;
  }

  function toast(message, type = 'success') {
    const node = document.createElement('div');
    node.className = `toast ${type}`;
    node.textContent = message;
    els.toastRoot.appendChild(node);
    setTimeout(() => {
      node.classList.add('leaving');
      setTimeout(() => node.remove(), 220);
    }, 3200);
  }

  function setRegisterBusy(busy) {
    els.registerBtn.disabled = busy;
    els.registerBtn.textContent = busy ? '注册中…' : '注册';
  }

  function setRefreshBusy(busy) {
    els.refreshBtn.disabled = busy;
    els.refreshBtn.textContent = busy ? '刷新中…' : '刷新';
  }

  function setPhoneRegisterBusy(busy) {
    els.phoneRegisterBtn.disabled = busy;
    els.phoneRegisterBtn.textContent = busy ? '注册中…' : '注册';
  }

  function setPhoneRefreshBusy(busy) {
    els.phoneRefreshBtn.disabled = busy;
    els.phoneRefreshBtn.textContent = busy ? '刷新中…' : '刷新';
  }

  function showEmpty(message) {
    els.body.innerHTML = '';
    const row = document.createElement('tr');
    const cell = document.createElement('td');
    cell.colSpan = 4;
    cell.className = 'empty-cell';
    cell.textContent = message;
    row.appendChild(cell);
    els.body.appendChild(row);
  }

  function renderSenders(highlightPhone = null) {
    els.body.innerHTML = '';
    els.count.textContent = String(state.senders.length);

    if (state.senders.length === 0) {
      showEmpty('还没有特殊发送方，先在上方注册。');
      return;
    }

    for (const sender of state.senders) {
      const row = document.createElement('tr');
      row.dataset.phone = sender.phone;
      if (highlightPhone && sender.phone === highlightPhone) {
        row.classList.add('highlight');
      }

      const phoneCell = document.createElement('td');
      phoneCell.dataset.label = '手机号';
      phoneCell.className = 'mono';
      phoneCell.textContent = sender.phone;

      const nameCell = document.createElement('td');
      nameCell.dataset.label = '名称';
      nameCell.textContent = sender.senderName;

      const codeCell = document.createElement('td');
      codeCell.dataset.label = 'API 代码';
      codeCell.className = 'code-cell';

      const codeText = document.createElement('code');
      codeText.className = 'code-text masked';
      codeText.textContent = maskCode(sender.apiCode);

      const toggleBtn = document.createElement('button');
      toggleBtn.type = 'button';
      toggleBtn.className = 'link-btn';
      toggleBtn.dataset.action = 'toggle';
      toggleBtn.textContent = '显示';

      const copyBtn = document.createElement('button');
      copyBtn.type = 'button';
      copyBtn.className = 'link-btn';
      copyBtn.dataset.action = 'copy';
      copyBtn.textContent = '复制';

      const codeLine = document.createElement('div');
      codeLine.className = 'code-line';
      codeLine.append(codeText, toggleBtn, copyBtn);
      codeCell.appendChild(codeLine);

      const actionCell = document.createElement('td');
      actionCell.dataset.label = '操作';
      actionCell.className = 'action-cell';

      const renameBtn = document.createElement('button');
      renameBtn.type = 'button';
      renameBtn.className = 'btn ghost small';
      renameBtn.dataset.action = 'rename';
      renameBtn.textContent = '改名';

      const deleteBtn = document.createElement('button');
      deleteBtn.type = 'button';
      deleteBtn.className = 'btn ghost small danger';
      deleteBtn.dataset.action = 'delete';
      deleteBtn.textContent = '删除';

      const actionButtons = document.createElement('div');
      actionButtons.className = 'action-buttons';
      actionButtons.append(renameBtn, deleteBtn);
      actionCell.appendChild(actionButtons);

      row.append(phoneCell, nameCell, codeCell, actionCell);
      els.body.appendChild(row);
    }
  }

  async function loadSenders(highlightPhone = null) {
    setRefreshBusy(true);
    try {
      const data = await request(API, { headers: { Accept: 'application/json' } });
      state.senders = Array.isArray(data) ? data : [];
      renderSenders(highlightPhone);
    } catch (error) {
      showEmpty('加载失败，请检查 server 是否运行。');
      els.count.textContent = '—';
      toast(error.message, 'error');
    } finally {
      setRefreshBusy(false);
    }
  }

  function showPhonesEmpty(message) {
    els.phonesBody.innerHTML = '';
    const row = document.createElement('tr');
    const cell = document.createElement('td');
    cell.colSpan = 2;
    cell.className = 'empty-cell';
    cell.textContent = message;
    row.appendChild(cell);
    els.phonesBody.appendChild(row);
  }

  function renderPhones(highlightPhone = null) {
    els.phonesBody.innerHTML = '';
    els.phoneCount.textContent = String(state.phones.length);

    if (state.phones.length === 0) {
      showPhonesEmpty('还没有登记手机号，先在上方注册。');
      return;
    }

    for (const phone of state.phones) {
      const row = document.createElement('tr');
      row.dataset.phone = phone;
      if (highlightPhone && phone === highlightPhone) {
        row.classList.add('highlight');
      }

      const phoneCell = document.createElement('td');
      phoneCell.dataset.label = '手机号';
      phoneCell.className = 'mono';
      phoneCell.textContent = phone;

      const actionCell = document.createElement('td');
      actionCell.dataset.label = '操作';
      actionCell.className = 'action-cell';

      const deleteBtn = document.createElement('button');
      deleteBtn.type = 'button';
      deleteBtn.className = 'btn ghost small danger';
      deleteBtn.dataset.action = 'delete';
      deleteBtn.textContent = '删除';

      const actionButtons = document.createElement('div');
      actionButtons.className = 'action-buttons';
      actionButtons.appendChild(deleteBtn);
      actionCell.appendChild(actionButtons);

      row.append(phoneCell, actionCell);
      els.phonesBody.appendChild(row);
    }
  }

  async function loadPhones(highlightPhone = null) {
    setPhoneRefreshBusy(true);
    try {
      const data = await request(API_PHONES, { headers: { Accept: 'application/json' } });
      state.phones = Array.isArray(data) ? data : [];
      renderPhones(highlightPhone);
    } catch (error) {
      showPhonesEmpty('加载失败，请检查 server 是否运行。');
      els.phoneCount.textContent = '—';
      toast(error.message, 'error');
    } finally {
      setPhoneRefreshBusy(false);
    }
  }

  async function copyText(text) {
    try {
      if (navigator.clipboard && window.isSecureContext) {
        await navigator.clipboard.writeText(text);
        return true;
      }
    } catch (error) {
      // Fall through to the legacy path below.
    }

    const helper = document.createElement('textarea');
    helper.value = text;
    helper.setAttribute('readonly', '');
    helper.style.position = 'fixed';
    helper.style.top = '-9999px';
    document.body.appendChild(helper);
    helper.select();

    let copied = false;
    try {
      copied = document.execCommand('copy');
    } catch (error) {
      copied = false;
    }
    helper.remove();
    return copied;
  }

  function findSender(phone) {
    return state.senders.find((item) => item.phone === phone) || null;
  }

  function openRename(sender) {
    state.editingPhone = sender.phone;
    els.renamePhone.textContent = `手机号 ${sender.phone}`;
    els.renameName.value = sender.senderName;
    els.dialog.showModal();
    els.renameName.focus();
    els.renameName.select();
  }

  function closeRename() {
    state.editingPhone = null;
    if (els.dialog.open) {
      els.dialog.close();
    }
  }

  els.form.addEventListener('submit', async (event) => {
    event.preventDefault();
    const phone = els.phone.value.trim();
    const senderName = els.senderName.value.trim();
    if (!phone || !senderName) {
      toast('手机号和名称都不能为空。', 'error');
      return;
    }

    setRegisterBusy(true);
    try {
      const data = await request(API, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
        body: JSON.stringify({ phone, senderName }),
      });
      els.senderName.value = '';
      toast('注册成功，已返回 API 代码。');
      await loadSenders(data && data.phone ? data.phone : phone);
    } catch (error) {
      toast(error.message, 'error');
    } finally {
      setRegisterBusy(false);
    }
  });

  els.refreshBtn.addEventListener('click', () => loadSenders());

  els.body.addEventListener('click', async (event) => {
    const button = event.target.closest('button[data-action]');
    if (!button) {
      return;
    }
    const row = button.closest('tr');
    if (!row) {
      return;
    }
    const sender = findSender(row.dataset.phone);
    if (!sender) {
      return;
    }

    if (button.dataset.action === 'rename') {
      openRename(sender);
      return;
    }

    if (button.dataset.action === 'delete') {
      const confirmed = window.confirm(
        `确定删除发送方「${sender.senderName}」（${sender.phone}）？删除后不可恢复，其 API 代码立即失效。`,
      );
      if (!confirmed) {
        return;
      }
      button.disabled = true;
      try {
        await request(`${API}?phone=${encodeURIComponent(sender.phone)}`, {
          method: 'DELETE',
          headers: { Accept: 'application/json' },
        });
        toast('已删除发送方。');
        await loadSenders();
      } catch (error) {
        toast(error.message, 'error');
      } finally {
        button.disabled = false;
      }
      return;
    }

    if (button.dataset.action === 'copy') {
      if (!sender.apiCode) {
        toast('该记录没有 API 代码。', 'error');
        return;
      }
      const copied = await copyText(sender.apiCode);
      toast(copied ? 'API 代码已复制。' : '复制失败，请手动选择代码。', copied ? 'success' : 'error');
      return;
    }

    if (button.dataset.action === 'toggle') {
      const codeText = row.querySelector('.code-text');
      const expanded = codeText.classList.toggle('masked') === false;
      codeText.textContent = expanded ? sender.apiCode : maskCode(sender.apiCode);
      button.textContent = expanded ? '隐藏' : '显示';
    }
  });

  els.renameCancel.addEventListener('click', closeRename);

  els.renameForm.addEventListener('submit', async (event) => {
    event.preventDefault();
    if (!state.editingPhone) {
      return;
    }
    const senderName = els.renameName.value.trim();
    if (!senderName) {
      toast('名称不能为空。', 'error');
      return;
    }

    const phone = state.editingPhone;
    els.renameSave.disabled = true;
    els.renameSave.textContent = '保存中…';
    try {
      await request(API, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
        body: JSON.stringify({ phone, senderName }),
      });
      closeRename();
      toast('名称已更新。');
      await loadSenders(phone);
    } catch (error) {
      toast(error.message, 'error');
    } finally {
      els.renameSave.disabled = false;
      els.renameSave.textContent = '保存';
    }
  });

  els.dialog.addEventListener('cancel', (event) => {
    event.preventDefault();
    closeRename();
  });

  els.phoneForm.addEventListener('submit', async (event) => {
    event.preventDefault();
    const phone = els.phoneInput.value.trim();
    if (!/^1[3-9]\d{9}$/.test(phone)) {
      toast('请输入正确的 11 位手机号。', 'error');
      return;
    }

    setPhoneRegisterBusy(true);
    try {
      await request(API_PHONES, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
        body: JSON.stringify({ phone }),
      });
      els.phoneInput.value = '';
      toast('手机号已登记。');
      await loadPhones(phone);
    } catch (error) {
      toast(error.message, 'error');
    } finally {
      setPhoneRegisterBusy(false);
    }
  });

  els.phoneRefreshBtn.addEventListener('click', () => loadPhones());

  els.phonesBody.addEventListener('click', async (event) => {
    const button = event.target.closest('button[data-action="delete"]');
    if (!button) {
      return;
    }
    const row = button.closest('tr');
    const phone = row && row.dataset.phone;
    if (!phone) {
      return;
    }
    const confirmed = window.confirm(`确定删除手机号 ${phone}？删除后该手机号将无法在 App 使用。`);
    if (!confirmed) {
      return;
    }

    button.disabled = true;
    try {
      await request(`${API_PHONES}?phone=${encodeURIComponent(phone)}`, {
        method: 'DELETE',
        headers: { Accept: 'application/json' },
      });
      toast('已删除手机号。');
      await loadPhones();
    } catch (error) {
      toast(error.message, 'error');
    } finally {
      button.disabled = false;
    }
  });

  function switchPanel(panelName) {
    document.querySelectorAll('.tab').forEach((tab) => {
      const active = tab.dataset.panel === panelName;
      tab.classList.toggle('active', active);
      tab.setAttribute('aria-selected', String(active));
    });
    document.getElementById('panel-phones').hidden = panelName !== 'phones';
    document.getElementById('panel-senders').hidden = panelName !== 'senders';
  }

  document.querySelectorAll('.tab').forEach((tab) => {
    tab.addEventListener('click', () => switchPanel(tab.dataset.panel));
  });

  els.serverUrl.textContent = window.location.origin;
  loadSenders();
  loadPhones();
})();
