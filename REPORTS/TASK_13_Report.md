# Task 13 Report: Infrastructure as Code (Ansible Automation)

**Project:** DevOps-Enabled Public Parking Availability Dashboard (ParkWatch)  
**Student:** Sajiya Shaikh  
**Roll Number:** 24102A2005  
**Milestone:** Task 13  
**Status:** **COMPLETED**  

---

## 1. Objective
Implement Infrastructure as Code (IaC) using Ansible to automate host provisioning, prerequisite management (GPG keyrings, official Docker apt repositories, container utilities), application directory structuring (`/opt/parkwatch`), reverse proxy templating, and idempotent orchestration.

---

## 2. Tools and Technologies Used
- **IaC Engine:** Ansible Core 2.20.1 / Ansible 13.1.0 on Python 3.12.3
- **Control Node & Execution Environment:** Ubuntu Linux (WSL2 environment on Windows 11 host)
- **Configuration Format:** YAML playbooks, Jinja2 dynamic templating (`docker/nginx.conf.j2`)
- **Modules Used:** `ansible.builtin.apt`, `ansible.builtin.get_url`, `ansible.builtin.command`, `ansible.builtin.apt_repository`, `ansible.builtin.group`, `ansible.builtin.user`, `ansible.builtin.file`, `ansible.builtin.template`, `ansible.builtin.systemd`

---

## 3. Work Performed
1. Configured Ansible environment inside WSL2 Ubuntu:
   - Installed `ansible` (core 2.20.1) and `python3-pip`.
   - Created `ansible/inventory.ini` configured for `localhost ansible_connection=local`.
   - Configured `ansible/ansible.cfg` with modern callback plugins (`stdout_callback = default`, `callbacks_enabled = timer, profile_tasks`).
2. Authored and modernized `ansible/playbook.yml`:
   - Prerequisite package management (`apt-transport-https`, `ca-certificates`, `curl`, `gnupg`).
   - Secure GPG key download and de-armoring into `/etc/apt/keyrings/docker.gpg` (replacing deprecated `apt_key` module).
   - Dynamic distribution codename detection with fallback to LTS (`noble`) for bleeding-edge or rolling releases.
   - Idempotent Docker Engine and Docker Compose plugin installation.
   - Non-root docker execution group configuration (`docker` group and user assignment).
   - Directory hierarchy creation: `/opt/parkwatch/app`, `/opt/parkwatch/nginx`, `/opt/parkwatch/data`, and `/opt/parkwatch/logs`.
   - Dynamic Jinja2 templating of Nginx reverse proxy configuration (`/opt/parkwatch/nginx/nginx.conf`).
   - Systemd execution safety check (`ansible_service_mgr == 'systemd'`) ensuring compatibility with WSL2 and container runtimes.
3. Successfully executed the playbook with 100% success rate (`ok=12, changed=3, failed=0`).

---

## 4. Files Created or Modified
- `ansible/playbook.yml`: Modernized idempotent host provisioning playbook.
- `ansible/inventory.ini`: Local connection inventory configuration.
- `ansible/ansible.cfg`: Modern callback and execution configuration.
- `ansible/group_vars/all.yml`: Configuration parameters and variables.
- `docs/ansible_documentation.md`: Comprehensive step-by-step documentation.

---

## 5. Commands Executed
```bash
# Execute the Ansible playbook from WSL2 Ubuntu control node
cd /mnt/c/Users/Admin/Desktop/Public-Parking-DevOps/ansible
ansible-playbook -i inventory.ini playbook.yml
```

### Execution Log / Terminal Output
```text
PLAY [Deploy ParkWatch Public Parking Dashboard] ******************************************************************************************************************************************************************

TASK [Gathering Facts] ********************************************************************************************************************************************************************************************
ok: [localhost]

TASK [Update apt cache and install prerequisites] *****************************************************************************************************************************************************************
ok: [localhost]

TASK [Create keyrings directory] **********************************************************************************************************************************************************************************
ok: [localhost]

TASK [Download Docker GPG key] ************************************************************************************************************************************************************************************
ok: [localhost]

TASK [Dearmor Docker GPG key] *************************************************************************************************************************************************************************************
ok: [localhost]

TASK [Add Docker repository] **************************************************************************************************************************************************************************************
ok: [localhost]

TASK [Install Docker Engine and Docker Compose plugin] ************************************************************************************************************************************************************
ok: [localhost]

TASK [Ensure docker service is started and enabled (systemd)] *****************************************************************************************************************************************************
skipping: [localhost]

TASK [Ensure docker group exists] *********************************************************************************************************************************************************************************
ok: [localhost]

TASK [Add current user to docker group] ***************************************************************************************************************************************************************************
ok: [localhost]

TASK [Create application directories] *****************************************************************************************************************************************************************************
changed: [localhost]

TASK [Create Nginx configuration directory] ***********************************************************************************************************************************************************************
changed: [localhost]

TASK [Deploy Nginx reverse proxy configuration template] **********************************************************************************************************************************************************
changed: [localhost]

PLAY RECAP ********************************************************************************************************************************************************************************************************
localhost                  : ok=12   changed=3    unreachable=0    failed=0    skipped=1    rescued=0    ignored=0
```

---

## 6. Actual Test Results & Verification Evidence
- **Total Tasks in Play:** 13
- **Tasks Passed (ok):** 12
- **Tasks Changed:** 3 (Directory creation and template generation)
- **Tasks Failed:** 0
- **Tasks Skipped:** 1 (Systemd service management safely skipped on WSL non-systemd target)
- **Idempotency Verified:** Re-running the playbook confirms existing packages and directories are not redundantly recreated.

---

## 7. Screenshots and Verification Artifacts
- **Playbook Play Recap Output:**  
  Verified from live terminal output:
  ```text
  PLAY RECAP ********************************************************************************************************************************************************************************************************
  localhost                  : ok=12   changed=3    unreachable=0    failed=0    skipped=1    rescued=0    ignored=0
  ```
  Target directories verified inside filesystem:
  `/opt/parkwatch/app`, `/opt/parkwatch/nginx`, `/opt/parkwatch/nginx/nginx.conf`.

---

## 8. Problems Encountered and Solutions
1. **Ubuntu Release Codename Mismatch:** WSL2 Ubuntu snapshot returned `plucky`, for which Docker CE repositories were not yet published.  
   *Solution:* Implemented fallback check in playbook using `noble` (Ubuntu 24.04 LTS) for apt repository registration.
2. **Systemd Service Manager in WSL:** Running `service` module in WSL without active systemd init caused `Service is in unknown state`.  
   *Solution:* Added `when: ansible_service_mgr == 'systemd'` condition so the task executes gracefully in both VM and WSL environments.
3. **Deprecated `apt_key` Module:** Replaced `apt_key` with modern `get_url` and `command: gpg --dearmor` writing to `/etc/apt/keyrings/docker.gpg`.

---

## 9. Final Status & Remaining Work
- **Status:** **COMPLETED**
- **Remaining Work:** None. Ansible configuration management playbook executes cleanly with 0 errors and idempotent behavior.
