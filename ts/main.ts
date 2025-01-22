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

class Main {

    projectsView: ProjectsView;
    memoriesView: MemoriesView;

    electron = require('electron');

    constructor() {

        this.projectsView = new ProjectsView();
        this.memoriesView = new MemoriesView();

        this.electron.ipcRenderer.send('get-theme');
        this.electron.ipcRenderer.on('set-theme', (event: Electron.IpcRendererEvent, theme: any) => {
            (document.getElementById('theme') as HTMLLinkElement).href = theme;
        });
        this.electron.ipcRenderer.on('show-projects', () => {
            this.showProjects();
        });
        this.electron.ipcRenderer.on('show-memories', () => {
            this.showMemories();
        });
        this.electron.ipcRenderer.on('set-size', () => {
            this.projectsView.setSizes();
            this.memoriesView.setSizes();
            this.setSizes();
        });
        this.electron.ipcRenderer.on('start-waiting', () => {
            document.body.classList.add("wait");
        });
        this.electron.ipcRenderer.on('end-waiting', () => {
            document.body.classList.remove("wait");
        });
        this.electron.ipcRenderer.on('set-status', (event: Electron.IpcRendererEvent, status: string) => {
            this.setStatus(status);
        });
        this.electron.ipcRenderer.on('generate-xliff', () => {
            this.projectsView.generateXLIFF();
        });
        this.electron.ipcRenderer.on('import-xliff',() =>{
            this.projectsView.importXLIFF();
        });
        document.getElementById('projectsButton').addEventListener('click', () => {
            this.showProjects();
        });
        document.getElementById('memoriesButton').addEventListener('click', () => {
            this.showMemories();
        });
        document.getElementById('updatesButton').addEventListener('click', () => {
            this.electron.ipcRenderer.send('check-updates');
        });
        document.getElementById('updatesButton').addEventListener('click', () => {
        });
        document.getElementById('settingsButton').addEventListener('click', () => {
            this.electron.ipcRenderer.send('show-settings');
        });
        document.getElementById('aboutButton').addEventListener('click', () => {
            this.electron.ipcRenderer.send('show-about');
        });
        this.showProjects();
        this.setSizes();
    }

    setSizes() {
        let top: number = document.getElementById('leftTop').clientHeight;
        let bottom: number = document.getElementById('leftBottom').clientHeight;
        let style: string = 'height: calc(100% - ' + (top + bottom + 8) + 'px);'; // add 8px bottom margin
        document.getElementById('leftCenter').setAttribute('style', style);
    }

    setStatus(status: string): void {
        let statusDiv: HTMLDivElement = document.getElementById('status') as HTMLDivElement;
        statusDiv.innerText = status;
        if (status.length > 0) {
            statusDiv.style.display = 'block';
        } else {
            statusDiv.style.display = 'none';
        }
    }

    showProjects(): void {
        document.getElementById('projectsButton').classList.add('selectedButton');
        document.getElementById('memoriesButton').classList.remove('selectedButton');
        this.memoriesView.hide();
        this.projectsView.show();
    }

    showMemories(): void {
        document.getElementById('projectsButton').classList.remove('selectedButton');
        document.getElementById('memoriesButton').classList.add('selectedButton');
        this.projectsView.hide();
        this.memoriesView.show();
    }
}

try {
    new Main();
} catch (error) {
    console.log(error);
}