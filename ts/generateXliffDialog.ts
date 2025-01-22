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

class GenerateXliffDialog {

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
                this.electron.ipcRenderer.send('close-generateXliff');
            }
            if (event.code === 'Enter') {
                this.generateXliff();
            }
        });
        this.electron.ipcRenderer.on('set-project', (event: Electron.IpcRendererEvent, arg: { projectId: number, description: string }) => {
            this.projectId = arg.projectId;
            document.getElementById('projectDescription').innerText = arg.description;
            this.electron.ipcRenderer.send('get-project-languages', arg.projectId);
        });
        this.electron.ipcRenderer.on('set-xliff-defaults', (event: Electron.IpcRendererEvent, arg: any) => {
            this.setDefaults(arg);
        });
        this.electron.ipcRenderer.on('set-project-languages', (event: Electron.IpcRendererEvent, arg: { srcLang: LanguageInterface, tgtLangs: LanguageInterface[] }) => {
            this.setLanguages(arg.tgtLangs);
        });
        document.getElementById('generateXliff').addEventListener('click', () => {
            this.generateXliff();
        });
        document.getElementById('folderBrowseButton').addEventListener('click', () => {
            this.electron.ipcRenderer.send('select-xliff-folder');
        });
        this.electron.ipcRenderer.on('set-xliff-folder', (event: Electron.IpcRendererEvent, folder: string) => {
            (document.getElementById('xliffFolder') as HTMLInputElement).value = folder;
        });
        document.getElementById('ditavalBrowseButton').addEventListener('click', () => {
            this.electron.ipcRenderer.send('select-ditaval');
        });
        this.electron.ipcRenderer.on('set-ditaval', (event: Electron.IpcRendererEvent, ditaval: string) => {
            (document.getElementById('ditavalFile') as HTMLInputElement).value = ditaval;
        });
        document.getElementById('xliffFolder').focus();
        this.setSizes();
    }

    setLanguages(langs: LanguageInterface[]): void {
        this.selectedLanguages = langs;
        let tableBody: HTMLTableSectionElement = document.getElementById('langsBody') as HTMLTableSectionElement;
        tableBody.innerHTML = '';
        for (let lang of langs) {
            let tr: HTMLTableRowElement = document.createElement('tr');
            let cell: HTMLTableCellElement = document.createElement('td');
            let checkbox: HTMLInputElement = document.createElement('input');
            checkbox.type = 'checkbox';
            checkbox.id = lang.code;
            checkbox.checked = true;
            cell.appendChild(checkbox);
            tr.appendChild(cell);
            cell = document.createElement('td');
            cell.classList.add('noWrap');
            cell.classList.add('fill_width');
            let label = document.createElement('label');
            label.htmlFor = lang.code;
            label.innerText = lang.description;
            cell.appendChild(label);
            tr.appendChild(cell);
            tableBody.appendChild(tr);
            checkbox.addEventListener('input', () => {
                if (checkbox.checked) {
                    this.selectedLanguages.push(lang);
                } else {
                    let index: number = this.selectedLanguages.indexOf(lang);
                    this.selectedLanguages.splice(index, 1);
                }
            });
        }
    }

    setSizes(): void {
        let leftContainer: HTMLDivElement = document.getElementById('leftContainer') as HTMLDivElement;
        let rightContainer: HTMLDivElement = document.getElementById('rightContainer') as HTMLDivElement;
        rightContainer.style.height = leftContainer.clientHeight + 'px';
        setTimeout(() => {
            this.electron.ipcRenderer.send('set-height', { window: 'generateXliff', width: document.body.clientWidth, height: document.body.clientHeight });
        }, 300);
    }

    generateXliff(): void {
        let folder: string = (document.getElementById('xliffFolder') as HTMLInputElement).value;
        if (folder === '') {
            this.electron.ipcRenderer.send('show-message', { type: 'warning', group: 'generateXliffDialog', key: 'selectXliffFolder' });
            return;
        }
        if (this.selectedLanguages.length === 0) {
            this.electron.ipcRenderer.send('show-message', { type: 'warning', group: 'generateXliffDialog', key: 'selectLanguages' });
            return;
        }
        let tgtLangs: string[] = [];
        for (let lang of this.selectedLanguages) {
            tgtLangs.push(lang.code);
        }
        let ditaval: string = (document.getElementById('ditavalFile') as HTMLInputElement).value;
        let version: string = '1.2';
        if ((document.getElementById('2.0') as HTMLInputElement).checked) {
            version = '2.0';
        }
        if ((document.getElementById('2.1') as HTMLInputElement).checked) {
            version = '2.1';
        }
        let reuseICE: boolean = (document.getElementById('reuseIce') as HTMLInputElement).checked;
        let modifiedOnly: boolean = (document.getElementById('modifiedOnly') as HTMLInputElement).checked;
        let useTM: boolean = (document.getElementById('useTm') as HTMLInputElement).checked;
        let paragrapSeg: boolean = (document.getElementById('paragraph') as HTMLInputElement).checked;
        let wordCount: boolean = (document.getElementById('wordCount') as HTMLInputElement).checked;
        let ignoreTracked: boolean = (document.getElementById('trackedChanges') as HTMLInputElement).checked;
        let ignoreSVG: boolean = (document.getElementById('ignoreSvg') as HTMLInputElement).checked;
        let embedSkeleton: boolean = (document.getElementById('embed') as HTMLInputElement).checked;
        this.electron.ipcRenderer.send('generate-xliff-file', {
            id: this.projectId,
            xliffFolder: folder,
            tgtLang: tgtLangs,
            ditaval: ditaval,
            version: version,
            useICE: reuseICE,
            modifiedFilesOnly: modifiedOnly,
            useTM: useTM,
            paragraph: paragrapSeg,
            generateCount: wordCount,
            ignoreTrackedChanges: ignoreTracked,
            ignoreSVG: ignoreSVG,
            embedSkeleton: embedSkeleton
        });
    }

    setDefaults(arg: any): void {
        if (arg.xliffFolder) {
            (document.getElementById('xliffFolder') as HTMLInputElement).value = arg.xliffFolder;
        }
        if (arg.ditaval) {
            (document.getElementById('ditavalFile') as HTMLInputElement).value = arg.ditaval;
        }
        if (arg.version === '1.2') {
            (document.getElementById('1.2') as HTMLInputElement).checked = true;
        }
        if (arg.version === '2.0') {
            (document.getElementById('2.0') as HTMLInputElement).checked = true;
        }
        if (arg.version === '2.1') {
            (document.getElementById('2.1') as HTMLInputElement).checked = true;
        }
        if (arg.useICE) {
            (document.getElementById('reuseIce') as HTMLInputElement).checked = true;
        }
        if (arg.modifiedFilesOnly) {
            (document.getElementById('modifiedOnly') as HTMLInputElement).checked = true;
        }
        if (arg.useTM) {
            (document.getElementById('useTm') as HTMLInputElement).checked = true;
        }
        if (arg.paragraph) {
            (document.getElementById('paragraph') as HTMLInputElement).checked = true;
        }
        if (arg.generateCount) {
            (document.getElementById('wordCount') as HTMLInputElement).checked = true;
        }
        if (arg.ignoreTrackedChanges) {
            (document.getElementById('trackedChanges') as HTMLInputElement).checked = true;
        }
        if (arg.ignoreSVG) {
            (document.getElementById('ignoreSvg') as HTMLInputElement).checked = true;
        }
        if (arg.embedSkeleton) {
            (document.getElementById('embed') as HTMLInputElement).checked = true;
        }
    }
}