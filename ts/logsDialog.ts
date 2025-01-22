/*******************************************************************************
 * Copyright (c) 2015-2025 Maxprograms.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-v10.html
 *
 * Contributors:
 *     Maxprograms - initial API and implementation
 *******************************************************************************/

class LogsDialog {

    electron = require('electron');

    logsArea: HTMLDivElement;
    pre: HTMLPreElement;

    constructor() {
        this.electron.ipcRenderer.send('get-theme');
        this.electron.ipcRenderer.on('set-theme', (event: Electron.IpcRendererEvent, theme: string) => {
            (document.getElementById('theme') as HTMLLinkElement).href = theme;
        });
        this.logsArea = document.getElementById('logs') as HTMLDivElement;
        this.pre = document.getElementById('pre') as HTMLPreElement;
        document.getElementById('cancel').addEventListener('click', () => {
            this.electron.ipcRenderer.send('cancel-process');
        });
        this.electron.ipcRenderer.on('set-data', (event: Electron.IpcRendererEvent, logs: string) => {
            this.pre.innerText += logs;
            this.logsArea.scrollTop = this.pre.scrollHeight;
        });
        this.electron.ipcRenderer.on('set-error', (event: Electron.IpcRendererEvent, logs: string) => {
            let span:HTMLSpanElement = document.createElement('span');
            span.setAttribute('style', 'color: red');
            span.textContent = logs;
            this.pre.appendChild(span);
            this.logsArea.scrollTop = this.pre.scrollHeight;
        });
        this.electron.ipcRenderer.on('hide-cancel', () => {
            document.getElementById('buttons').style.display = 'none';
            (document.getElementById('cancel') as HTMLButtonElement).disabled = true; 
            this.setSizes();
        });
        window.addEventListener('resize', () => {
            this.setSizes();
        });
        this.setSizes();
    }

    setSizes() {
        let buttonsArea: HTMLDialogElement = document.getElementById('buttons') as HTMLDialogElement;
        this.logsArea.style.height = (window.innerHeight - buttonsArea.clientHeight - 16) + 'px';
    }
}