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

class ProjectsView {

    electron = require('electron');

    container: HTMLDivElement;
    tableContainer: HTMLDivElement;
    tbody: HTMLTableSectionElement;
    selectedProjects: number[];
    projectSortFielD: string = 'description';
    projectSortAscending: boolean = true;
    projects: Project[];
    home: string;
    lang: string;
    statusMap: Map<string, string>;

    constructor() {
        this.selectedProjects = [];
        this.tbody = document.getElementById('projectsTableBody') as HTMLTableSectionElement;
        this.container = document.getElementById('projectsView') as HTMLDivElement;
        this.tableContainer = document.getElementById('projectsTableContainer') as HTMLDivElement;
        this.setSizes();

        this.container.addEventListener('drop', (event: DragEvent) => { this.dropListener(event, this.container) });
        this.container.addEventListener('dragover', (event: DragEvent) => { this.dragOverListener(event) });
        this.container.addEventListener('dragenter', () => { this.dragEnterListener(this.container) });
        this.container.addEventListener('dragleave', () => { this.dragLeaveListener(this.container) });

        this.electron.ipcRenderer.send('get-projects');
        this.electron.ipcRenderer.on('set-projects', (event: any, arg: { projects: Project[], home: string, lang: string, statusMap: Map<string, string> }) => {
            this.projects = arg.projects;
            this.lang = arg.lang;
            this.statusMap = arg.statusMap;
            this.home = arg.home;
            this.displayProjects();
        });
        this.electron.ipcRenderer.on('request-remove-project', () => {
            this.removeProjects();
        });
        this.electron.ipcRenderer.on('edit-project', () => {
            this.editProject();
        });
        document.getElementById('selectAllProjects').addEventListener('click', () => {
            this.selectAllProjects((document.getElementById('selectAllProjects') as HTMLInputElement).checked);
        });
        document.getElementById('addProject').addEventListener('click', () => {
            this.electron.ipcRenderer.send('show-add-project');
        });
        document.getElementById('editProject').addEventListener('click', () => {
            this.editProject();
        });
        this.electron.ipcRenderer.on('project-info', () => {
            this.projectStatus();
        });
        document.getElementById('removeProject').addEventListener('click', () => {
            if (this.selectedProjects.length === 0) {
                this.electron.ipcRenderer.send('show-message', { type: 'warning', group: 'projectsView', key: 'selectProject' });
                return;
            }
            this.electron.ipcRenderer.send('remove-projects', this.selectedProjects);
        });
        document.getElementById('projectStatus').addEventListener('click', () => {
            this.projectStatus();
        });
        document.getElementById('generateXLIFF').addEventListener('click', () => {
            this.generateXLIFF();
        });
        document.getElementById('importXLIFF').addEventListener('click', () => {
            this.importXLIFF();
        });
        document.getElementById('project-description').addEventListener('click', () => {
            (document.getElementById('project-' + this.projectSortFielD) as HTMLTableCellElement).classList.remove('arrow-down');
            (document.getElementById('project-' + this.projectSortFielD) as HTMLTableCellElement).classList.remove('arrow-up');
            if (this.projectSortFielD === 'description') {
                this.projectSortAscending = !this.projectSortAscending;
            } else {
                this.projectSortFielD = 'description';
                this.projectSortAscending = true;
            }
            this.displayProjects();
        });
        document.getElementById('project-map').addEventListener('click', () => {
            (document.getElementById('project-' + this.projectSortFielD) as HTMLTableCellElement).classList.remove('arrow-down');
            (document.getElementById('project-' + this.projectSortFielD) as HTMLTableCellElement).classList.remove('arrow-up');
            if (this.projectSortFielD === 'map') {
                this.projectSortAscending = !this.projectSortAscending;
            } else {
                this.projectSortFielD = 'map';
                this.projectSortAscending = true;
            }
            this.displayProjects();
        });
        document.getElementById('project-srcLang').addEventListener('click', () => {
            (document.getElementById('project-' + this.projectSortFielD) as HTMLTableCellElement).classList.remove('arrow-down');
            (document.getElementById('project-' + this.projectSortFielD) as HTMLTableCellElement).classList.remove('arrow-up');
            if (this.projectSortFielD === 'srcLang') {
                this.projectSortAscending = !this.projectSortAscending;
            } else {
                this.projectSortFielD = 'srcLang';
                this.projectSortAscending = true;
            }
            this.displayProjects();
        });
        document.getElementById('project-status').addEventListener('click', () => {
            (document.getElementById('project-' + this.projectSortFielD) as HTMLTableCellElement).classList.remove('arrow-down');
            (document.getElementById('project-' + this.projectSortFielD) as HTMLTableCellElement).classList.remove('arrow-up');
            if (this.projectSortFielD === 'status') {
                this.projectSortAscending = !this.projectSortAscending;
            } else {
                this.projectSortFielD = 'status';
                this.projectSortAscending = true;
            }
            this.displayProjects();
        });
        document.getElementById('project-created').addEventListener('click', () => {
            (document.getElementById('project-' + this.projectSortFielD) as HTMLTableCellElement).classList.remove('arrow-down');
            (document.getElementById('project-' + this.projectSortFielD) as HTMLTableCellElement).classList.remove('arrow-up');
            if (this.projectSortFielD === 'created') {
                this.projectSortAscending = !this.projectSortAscending;
            } else {
                this.projectSortFielD = 'created';
                this.projectSortAscending = true;
            }
            this.displayProjects();
        });
        document.getElementById('project-updated').addEventListener('click', () => {
            (document.getElementById('project-' + this.projectSortFielD) as HTMLTableCellElement).classList.remove('arrow-down');
            (document.getElementById('project-' + this.projectSortFielD) as HTMLTableCellElement).classList.remove('arrow-up');
            if (this.projectSortFielD === 'updated') {
                this.projectSortAscending = !this.projectSortAscending;
            } else {
                this.projectSortFielD = 'updated';
                this.projectSortAscending = true;
            }
            this.displayProjects();
        });
    }

    removeProjects(): void {
        if (this.selectedProjects.length === 0) {
            this.electron.ipcRenderer.send('show-message', { type: 'warning', group: 'projectsView', key: 'selectProject' });
            return;
        }
        this.electron.ipcRenderer.send('remove-projects', this.selectedProjects);
        this.selectedProjects = [];
    }

    editProject(): void {
        if (this.selectedProjects.length === 0) {
            this.electron.ipcRenderer.send('show-message', { type: 'warning', group: 'projectsView', key: 'selectProject' });
            return;
        }
        if (this.selectedProjects.length > 1) {
            this.electron.ipcRenderer.send('show-message', { type: 'warning', group: 'projectsView', key: 'selectOneProject' });
            return;
        }
        this.electron.ipcRenderer.send('edit-selected-project', this.selectedProjects[0]);
    }

    projectStatus(): void {
        if (this.selectedProjects.length === 0) {
            this.electron.ipcRenderer.send('show-message', { type: 'warning', group: 'projectsView', key: 'selectProject' });
            return;
        }
        if (this.selectedProjects.length > 1) {
            this.electron.ipcRenderer.send('show-message', { type: 'warning', group: 'projectsView', key: 'selectOneProject' });
            return;
        }
        this.electron.ipcRenderer.send('show-project-status', this.selectedProjects[0]);
    }

    generateXLIFF(): void {
        if (this.selectedProjects.length === 0) {
            this.electron.ipcRenderer.send('show-message', { type: 'warning', group: 'projectsView', key: 'selectProject' });
            return;
        }
        if (this.selectedProjects.length > 1) {
            this.electron.ipcRenderer.send('show-message', { type: 'warning', group: 'projectsView', key: 'selectOneProject' });
            return;
        }
        for (let project of this.projects) {
            if (project.id === this.selectedProjects[0]) {
                console.log(JSON.stringify(project, null, 2));
                this.electron.ipcRenderer.send('show-generate-xliff', { projectId: project.id, description: project.title });
                return;
            }
        }
    }

    importXLIFF(): void {
        if (this.selectedProjects.length === 0) {
            this.electron.ipcRenderer.send('show-message', { type: 'warning', group: 'projectsView', key: 'selectProject' });
            return;
        }
        if (this.selectedProjects.length > 1) {
            this.electron.ipcRenderer.send('show-message', { type: 'warning', group: 'projectsView', key: 'selectOneProject' });
            return;
        }
        for (let project of this.projects) {
            if (project.id === this.selectedProjects[0]) {
                console.log(JSON.stringify(project, null, 2));
                this.electron.ipcRenderer.send('show-import-xliff', { projectId: project.id, description: project.title });
                return;
            }
        }
    }

    displayProjects(): void {
        this.selectedProjects = [];
        if (this.projectSortAscending) {
            (document.getElementById('project-' + this.projectSortFielD) as HTMLTableCellElement).classList.add('arrow-up');
        } else {
            (document.getElementById('project-' + this.projectSortFielD) as HTMLTableCellElement).classList.add('arrow-down');
        }
        this.projects.sort((a: Project, b: Project) => {
            let x: string;
            let y: string;
            if (this.projectSortFielD === 'description') {
                x = a.title.toLocaleLowerCase(this.lang)
                y = b.title.toLocaleLowerCase(this.lang)
            } else if (this.projectSortFielD === 'map') {
                x = a.map.toLocaleLowerCase(this.lang)
                y = b.map.toLocaleLowerCase(this.lang)
            } else if (this.projectSortFielD === 'status') {
                x = this.statusMap.get(a.status);
                y = this.statusMap.get(b.status);
            } else if (this.projectSortFielD === 'srcLang') {
                x = a.srcLanguage;
                y = b.srcLanguage;
            } else if (this.projectSortFielD === 'created') {
                x = a.creationDate;
                y = b.creationDate;
            } else if (this.projectSortFielD === 'updated') {
                x = a.lastUpdate;
                y = b.lastUpdate;
            }
            if (x < y) { return -1; }
            if (x > y) { return 1; }
            return 0;
        });
        if (!this.projectSortAscending) {
            this.projects.reverse();
        }
        this.tbody.innerHTML = '';
        for (let project of this.projects) {
            let row: HTMLTableRowElement = document.createElement('tr');
            this.tbody.appendChild(row);

            let cell: HTMLTableCellElement = document.createElement('td');
            cell.classList.add('center');
            let checkbox: HTMLInputElement = document.createElement('input');
            checkbox.type = 'checkbox';
            checkbox.id = 'project' + project.id;
            checkbox.addEventListener('click', (event) => {
                if (checkbox.checked) {
                    this.selectedProjects.push(project.id);
                } else {
                    this.selectedProjects = this.selectedProjects.filter((value: number) => {
                        return value !== project.id;
                    });
                }
                event.stopPropagation();
            });
            checkbox.checked = this.selectedProjects.includes(project.id);
            cell.appendChild(checkbox);
            row.appendChild(cell);

            cell = document.createElement('td');
            cell.textContent = project.title;
            row.appendChild(cell);

            cell = document.createElement('td');
            if (project.map.startsWith(this.home)) {
                cell.textContent = '~' + project.map.substring(this.home.length);
            } else {
                cell.textContent = project.map;
            }
            row.appendChild(cell);

            cell = document.createElement('td');
            cell.classList.add('center');
            cell.textContent = project.srcLanguage;
            row.appendChild(cell);

            cell = document.createElement('td');
            cell.classList.add('center');
            cell.textContent = this.statusMap.get(project.status);
            row.appendChild(cell);

            cell = document.createElement('td');
            cell.classList.add('center');
            cell.textContent = project.creationDate;
            row.appendChild(cell);

            cell = document.createElement('td');
            cell.classList.add('center');
            cell.textContent = project.lastUpdate;
            row.appendChild(cell);

            row.addEventListener('click', () => {
                checkbox.checked = !checkbox.checked;
                if (checkbox.checked) {
                    this.selectedProjects.push(project.id);
                } else {
                    this.selectedProjects = this.selectedProjects.filter((value: number) => {
                        return value !== project.id;
                    });
                }
            });
        }
    }

    selectAllProjects(checked: boolean): void {
        let checkboxes: NodeListOf<HTMLInputElement> = document.querySelectorAll('input[type="checkbox"]');
        this.selectedProjects = [];
        for (let checkbox of checkboxes) {
            if (checkbox.id.startsWith('project')) {
                checkbox.checked = checked;
                if (checked) {
                    this.selectedProjects.push(parseInt(checkbox.id.substring(7)));
                }
            }
        }
    }

    hide(): void {
        this.container.classList.add('hidden');
    }

    show(): void {
        this.container.classList.remove('hidden');
    }

    setSizes(): void {
        let rightSide: HTMLDivElement = document.getElementById('rightSide') as HTMLDivElement;
        let projectsTopBar: HTMLDivElement = document.getElementById('projectsTopBar') as HTMLDivElement;
        this.tableContainer.style.height = (rightSide.clientHeight - projectsTopBar.clientHeight) + 'px';
        this.tableContainer.style.width = (document.body.clientWidth - document.getElementById('leftSide').clientWidth) + 'px';
    }

    dropListener(event: DragEvent, container: HTMLElement): void {
        event.preventDefault();
        event.stopPropagation();
        let filesList: string[] = [];
        for (const f of event.dataTransfer.files) {
            filesList.push(this.electron.webUtils.getPathForFile(f));
        }
        if (filesList.length === 1) {
            this.electron.ipcRenderer.send('files-dropped', filesList[0]);
        }
        container.style.opacity = '1';
    }

    dragEnterListener(container: HTMLElement): void {
        container.style.opacity = '0.3';
    }

    dragLeaveListener(container: HTMLElement): void {
        container.style.opacity = '1';
    }

    dragOverListener(event: DragEvent): void {
        event.preventDefault();
        event.stopPropagation();
        event.dataTransfer.dropEffect = 'link';
    }
}
