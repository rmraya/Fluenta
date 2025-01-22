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

class SystemInformation {

    electron = require('electron');

    constructor() {
        this.electron.ipcRenderer.send('get-theme');
        this.electron.ipcRenderer.on('set-theme', (event: Electron.IpcRendererEvent, theme: string) => {
            (document.getElementById('theme') as HTMLLinkElement).href = theme;
            this.electron.ipcRenderer.send('get-system-info');
        });
        document.addEventListener('keydown', (event: KeyboardEvent) => {
            if (event.code === 'Escape') {
                this.electron.ipcRenderer.send('close-system-info');
            }
        });
        this.electron.ipcRenderer.on('set-system-info', (event: Electron.IpcRendererEvent, arg: any) => {
            this.setVersions(arg);
            setTimeout(() => {
                this.electron.ipcRenderer.send('set-height', { window: 'systemInfo', width: document.body.clientWidth, height: document.body.clientHeight });
            }, 300);
        });
      
    }

    setVersions(arg: any) {
        document.getElementById('fluenta').innerText = arg.version + '-' + arg.build;
        document.getElementById('swordfish').innerText = arg.swordfish;
        document.getElementById('openxliff').innerText = arg.openxliff;
        document.getElementById('electron').innerText = arg.electron;
        document.getElementById('xmljava').innerText = arg.xmljava;
        document.getElementById('bcp47j').innerText = arg.bcp47j;
        document.getElementById('java').innerText = arg.java + ' (' + arg.vendor + ')';
    }
}