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

class EditProjectDialog {

    electron = require('electron');

    tgtLangs: LanguageInterface[] = [];
    selectedLanguages: LanguageInterface[] = [];
    memories: number[] = [];
    project: Project;

    constructor() {
        this.electron.ipcRenderer.send('get-theme');
        this.electron.ipcRenderer.on('set-theme', (event: Electron.IpcRendererEvent, theme: string) => {
            (document.getElementById('theme') as HTMLLinkElement).href = theme;
            this.electron.ipcRenderer.send('get-languages');
        });
        this.electron.ipcRenderer.on('set-languages', (event: Electron.IpcRendererEvent, languages: LanguageInterface[]) => {
            this.setLanguages(languages);
            setTimeout(() => {
                this.electron.ipcRenderer.send('set-height', { window: 'editProjectDialog', width: document.body.clientWidth, height: document.body.clientHeight });
            }, 300);
        });
        this.electron.ipcRenderer.on('set-project', (event: Electron.IpcRendererEvent, project: Project) => {
            this.setProject(project);
        });
        document.addEventListener('keydown', (event: KeyboardEvent) => {
            if (event.code === 'Escape') {
                this.electron.ipcRenderer.send('close-projectDialog');
            }
            if (event.code === 'Enter') {
                this.updateProject();
            }
        });
        document.getElementById('browseButton').addEventListener('click', () => {
            this.electron.ipcRenderer.send('get-ditamap');
        });
        this.electron.ipcRenderer.on('set-ditamap', (event: Electron.IpcRendererEvent, file: string) => {
            (document.getElementById('ditaMap') as HTMLInputElement).value = file;
        });
        document.getElementById('addTarget').addEventListener('click', () => {
            this.electron.ipcRenderer.send('add-target-language', 'editProjectDialog');
        });
        document.getElementById('removeTarget').addEventListener('click', () => {
            this.removeTargetLanguages();
        });
        document.getElementById('updateButton').addEventListener('click', () => {
            this.updateProject();
        });
        this.electron.ipcRenderer.on('add-language', (event: Electron.IpcRendererEvent, arg: LanguageInterface) => {
            this.tgtLangs.push(arg);
            this.displayTargetLanguages();
        });
        this.electron.ipcRenderer.on('set-project-languages', (event: Electron.IpcRendererEvent, arg: { srcLang: LanguageInterface, tgtLangs: LanguageInterface[] }) => {
            this.tgtLangs = arg.tgtLangs;
            (document.getElementById('srcLangSelect') as HTMLSelectElement).value = arg.srcLang.code;
            this.displayTargetLanguages();
        });
        document.getElementById('selectMemories').addEventListener('click', () => {
            this.electron.ipcRenderer.send('select-memories', { dialog: 'editProjectDialog', memories: this.memories });
        });

        this.electron.ipcRenderer.on('set-memories', (event: Electron.IpcRendererEvent, memories: number[]) => {
            this.memories = memories;
        });
        document.getElementById('ditaMap').focus();
    }

    removeTargetLanguages() {
        if (this.selectedLanguages.length === 0) {
            this.electron.ipcRenderer.send('show-message', { type: 'warning', group: 'projectDialog', key: 'selectTargetLanguage' });
            return
        }
        for (let language of this.selectedLanguages) {
            let index: number = this.tgtLangs.indexOf(language);
            this.tgtLangs.splice(index, 1);
        }
        this.selectedLanguages = [];
        this.displayTargetLanguages();
    }

    setLanguages(languages: LanguageInterface[]): void {
        let select: HTMLSelectElement = document.getElementById('srcLangSelect') as HTMLSelectElement;
        for (let language of languages) {
            let option: HTMLOptionElement = document.createElement('option');
            option.value = language.code;
            option.textContent = language.description;
            select.appendChild(option);
        }
    }

    displayTargetLanguages(): void {
        this.tgtLangs.sort((a: LanguageInterface, b: LanguageInterface) => {
            return a.description.localeCompare(b.description, document.documentElement.lang);
        });
        let targetLanguages: HTMLTableSectionElement = document.getElementById('targetLanguages') as HTMLTableSectionElement;
        targetLanguages.innerHTML = '';
        for (let language of this.tgtLangs) {
            let row: HTMLTableRowElement = document.createElement('tr');
            targetLanguages.appendChild(row);
            let cell: HTMLTableCellElement = document.createElement('td');
            cell.style.width = '20px';
            let checkBox: HTMLInputElement = document.createElement('input');
            checkBox.type = 'checkbox';
            checkBox.id = language.code;
            cell.appendChild(checkBox);
            row.appendChild(cell);
            cell = document.createElement('td');
            let label: HTMLLabelElement = document.createElement('label');
            label.htmlFor = language.code;
            label.innerText = language.description;
            cell.appendChild(label);
            row.appendChild(cell);
            checkBox.addEventListener('input', () => {
                if (checkBox.checked) {
                    this.selectedLanguages.push(language);
                } else {
                    let index: number = this.selectedLanguages.indexOf(language);
                    this.selectedLanguages.splice(index, 1);
                }
            });
        }
    }

    setProject(project: Project) {
        this.project = project;
        this.memories = project.memories;
        (document.getElementById('ditaMap') as HTMLInputElement).value = project.map;
        (document.getElementById('nameInput') as HTMLInputElement).value = project.title;
        (document.getElementById('descriptionInput') as HTMLTextAreaElement).value = project.description;
        (document.getElementById('srcLangSelect') as HTMLSelectElement).value = project.srcLanguage;
        this.electron.ipcRenderer.send('get-project-languages', this.project.id);
    }

    updateProject(): void {
        let map: string = (document.getElementById('ditaMap') as HTMLInputElement).value;
        if (map === '') {
            this.electron.ipcRenderer.send('show-message', { type: 'warning', group: 'projectDialog', key: 'selectDitamap' });
            return;
        }
        let title: string = (document.getElementById('nameInput') as HTMLInputElement).value;
        if (title === '') {
            this.electron.ipcRenderer.send('show-message', { type: 'warning', group: 'projectDialog', key: 'enterName' });
            return;
        }
        if (this.tgtLangs.length === 0) {
            this.electron.ipcRenderer.send('show-message', { type: 'warning', group: 'projectDialog', key: 'addTargetLanguage' });
            return;
        }
        this.project.title = title;
        this.project.description = (document.getElementById('descriptionInput') as HTMLTextAreaElement).value;
        this.project.srcLanguage = (document.getElementById('srcLangSelect') as HTMLSelectElement).value;
        let tgtLanguages: string[] = [];
        for (let language of this.tgtLangs) {
            tgtLanguages.push(language.code);
        }
        this.project.tgtLanguages = tgtLanguages;
        this.project.memories = this.memories;
        this.project.map = map;
        let newHistory: StatusEvent[] = [];
        for (let entry of this.project.history) {
            let found: boolean = false;
            for (let language of this.tgtLangs) {
                if (entry.language === language.code) {
                    found = true;
                    break;
                }
            }
            if (found) {
                newHistory.push(entry);
            }
        }
        this.project.history = newHistory;
        let newLanguageStatus: any = {};
        for (let language of this.tgtLangs) {
            if (this.project.languageStatus[language.code] === undefined) {
                newLanguageStatus[language.code] = '0';
            } else {
                newLanguageStatus[language.code] = this.project.languageStatus[language.code];
            }
        }
        this.project.languageStatus = newLanguageStatus;           
        this.electron.ipcRenderer.send('update-project', this.project);
    }
}