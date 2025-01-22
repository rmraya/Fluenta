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

class Updates {

    electron = require('electron');

    constructor() {
        this.electron.ipcRenderer.send('get-theme');
        this.electron.ipcRenderer.on('set-theme', (event: Electron.IpcRendererEvent, theme: string) => {
            (document.getElementById('theme') as HTMLLinkElement).href = theme;
            this.electron.ipcRenderer.send('get-update-versions');
        });
        this.electron.ipcRenderer.on('set-update-versions', (event: Electron.IpcRendererEvent, arg: { current: string, latest: string }) => {
            this.setVersions(arg);
           
        });
        document.addEventListener('keydown', (event: KeyboardEvent) => {
            if (event.code === 'Escape') {
                this.electron.ipcRenderer.send('close-updates');
            }
        });
        document.getElementById('release').addEventListener('click', () => {
            this.electron.ipcRenderer.send('show-release-history');
        });
        document.getElementById('download').addEventListener('click', () => {
            this.electron.ipcRenderer.send('download-update');
        });
        setTimeout(() => {
            this.electron.ipcRenderer.send('set-height', { window: 'updates', width: document.body.clientWidth, height: document.body.clientHeight });
        }, 300);
    }

    setVersions(arg: { current: string; latest: string; }) {
        document.getElementById('current').innerText = arg.current;
        document.getElementById('latest').innerText = arg.latest
    }
}