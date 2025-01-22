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

class ImportXliffDialog {

    electron = require('electron');
    projectId: number;
    selectedLanguages: LanguageInterface[] = [];

    constructor() {
        this.electron.ipcRenderer.send('get-theme');
        this.electron.ipcRenderer.on('set-theme', (event: Electron.IpcRendererEvent, theme: string) => {
            (document.getElementById('theme') as HTMLLinkElement).href = theme;
        });
        document.addEventListener('keydown', (event: KeyboardEvent) => {
            if (event.code === 'Escape') {
                this.electron.ipcRenderer.send('close-importXliff');
            }
            if (event.code === 'Enter') {
                this.importXliff();
            }
        });
        this.electron.ipcRenderer.on('set-project', (event: Electron.IpcRendererEvent, arg: { projectId: number, description: string }) => {
            this.projectId = arg.projectId;
            document.getElementById('projectDescription').innerText = arg.description;
            this.electron.ipcRenderer.send('get-project-languages', arg.projectId);
            this.electron.ipcRenderer.send('get-import-defaults', arg.projectId);
        });
        document.getElementById('fileBrowseButton').addEventListener('click', () => {
            this.electron.ipcRenderer.send('select-xliff-file');
        });
        this.electron.ipcRenderer.on('set-xliff-file', (event: Electron.IpcRendererEvent, file: string) => {
            (document.getElementById('xliffFile') as HTMLInputElement).value = file;
        });
        document.getElementById('folderBrowseButton').addEventListener('click', () => {
            this.electron.ipcRenderer.send('select-output-folder');
        });
        this.electron.ipcRenderer.on('set-output-folder', (event: Electron.IpcRendererEvent, folder: string) => {
            (document.getElementById('outputFolder') as HTMLInputElement).value = folder;
        });
        document.getElementById('importXliff').addEventListener('click', () => {
            this.importXliff();
        });
        setTimeout(() => {
            this.electron.ipcRenderer.send('set-height', { window: 'importXliffDialog', width: document.body.clientWidth, height: document.body.clientHeight });
        }, 500);
    }

    importXliff() {
        let xliffFile: string = (document.getElementById('xliffFile') as HTMLInputElement).value;
        if (xliffFile === '') {
            this.electron.ipcRenderer.send('show-message', { type: 'warning', group: 'importXliffDialog', key: 'selectXliffFile' });
            return;
        }
        let outputFolder: string = (document.getElementById('outputFolder') as HTMLInputElement).value;
        if (outputFolder === '') {
            this.electron.ipcRenderer.send('show-message', { type: 'warning', group: 'importXliffDialog', key: 'selectOutputFolder' });
            return;
        } let updateMemory: boolean = (document.getElementById('updateMemory') as HTMLInputElement).checked;
        let acceptUnapproved: boolean = (document.getElementById('acceptUnapproved') as HTMLInputElement).checked;
        let ignoreTagErrors: boolean = (document.getElementById('ignoreTagErrors') as HTMLInputElement).checked;
        this.electron.ipcRenderer.send('import-xliff-file', {
            id: this.projectId,
            xliffFile: xliffFile,
            outputFolder: outputFolder,
            updateTM: updateMemory,
            acceptUnapproved: acceptUnapproved,
            ignoreTagErrors: ignoreTagErrors
        });
    }
}