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

class AddTargetLanguage {

    electron = require('electron');

    constructor() {
        this.electron.ipcRenderer.send('get-theme');
        this.electron.ipcRenderer.on('set-theme', (event: Electron.IpcRendererEvent, theme: string) => {
            (document.getElementById('theme') as HTMLLinkElement).href = theme;
            this.electron.ipcRenderer.send('get-languages');
        });

        this.electron.ipcRenderer.on('set-languages', (event: Electron.IpcRendererEvent, languages: LanguageInterface[]) => {
            this.setLanguages(languages);
            setTimeout(() => {
                this.electron.ipcRenderer.send('set-height', { window: 'addTargetLanguage', width: document.body.clientWidth, height: document.body.clientHeight });
            }, 300);
        });
        document.addEventListener('keydown', (event: KeyboardEvent) => {
            if (event.code === 'Escape') {
                this.electron.ipcRenderer.send('close-addTargetLanguage');
            }
            if (event.code === 'Enter') {
                this.addTargetLanguage();
            }
        });
        document.getElementById('addTargetLanguage').addEventListener('click', () => {
            this.addTargetLanguage();
        });
        (document.getElementById('tgtLangSelect') as HTMLSelectElement).focus();
    }

    addTargetLanguage(): void {
        let select: HTMLSelectElement = document.getElementById('tgtLangSelect') as HTMLSelectElement;
        this.electron.ipcRenderer.send('set-target-language', select.value );
    }

    setLanguages(languages: LanguageInterface[]): void {
        let select: HTMLSelectElement = document.getElementById('tgtLangSelect') as HTMLSelectElement;
        for (let language of languages) {
            let option: HTMLOptionElement = document.createElement('option');
            option.value = language.code;
            option.textContent = language.description;
            select.appendChild(option);
        }
    }
}