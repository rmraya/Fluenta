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

class AboutDialog {

    electron = require('electron');

    constructor() {
        this.electron.ipcRenderer.send('get-theme');
        this.electron.ipcRenderer.on('set-theme', (event: Electron.IpcRendererEvent, theme: string) => {
            (document.getElementById('theme') as HTMLLinkElement).href = theme;
        });
        document.addEventListener('keydown', (event: KeyboardEvent) => {
            if (event.code === 'Escape') {
                this.electron.ipcRenderer.send('close-aboutDialog');
            }
        });
        this.electron.ipcRenderer.send('get-version');
        this.electron.ipcRenderer.on('set-version', (event: Electron.IpcRendererEvent, arg: string) => {
            document.getElementById('version').textContent = arg;
        });
        document.getElementById('systemInfo').addEventListener('click', () => {
            this.electron.ipcRenderer.send('show-system');
        });
        document.getElementById('licenses').addEventListener('click', () => {
            this.electron.ipcRenderer.send('show-licenses', 'aboutDialog');
        });
        setTimeout(() => {
            this.electron.ipcRenderer.send('set-height', { window: 'aboutDialog', width: document.body.clientWidth, height: document.body.clientHeight });
        }, 500);
    }
}