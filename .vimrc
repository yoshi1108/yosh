set viminfo='50,<2000,s100,:0,

" ��unite
nnoremap    [unite]   <Nop>
nmap    <Leader>f [unite]
nnoremap [unite]u  :<C-u>Unite -no-split<Space>
nnoremap <silent> [unite]f :<C-u>Unite<Space>buffer<CR>
nnoremap <silent> [unite]b :<C-u>Unite<Space>bookmark<CR>
nnoremap <silent> [unite]m :<C-u>Unite<Space>file_mru<CR>
nnoremap <silent> [unite]r :<C-u>UniteWithBufferDir file<CR>
nnoremap <silent> ,vr :UniteResume<CR>

" �� �^�u����
map <C-TAB> :tabnext<CR>

"map <C-w> :tabclose<CR>
map <C-n> :tabnew<CR>

" ���C���N�������^���T�[�`
set incsearch

" �� Ctrl-space�ŕ⊮
if has('unix') 
    inoremap <Nul> <C-n>
elseif has('win32')
    inoremap <C-Space> <C-n>
endif

" �� perltidy
map ,pt ! perl c:\Perl\bin\perltidy.pl -st<CR>

" �� neobundle
set nocompatible
if has('vim_starting')
	set runtimepath+=C:/tools/vim74-kaoriya-win32/plugins/neobundle.vim-master/plugin/neobundle.vim
endif
call neobundle#rc(expand('C:/tools/vim74-kaoriya-win32/bundles/'))
NeoBundle 'Shougo/vimshell'
NeoBundle 'Shougo/unite.vim'
NeoBundle 'Shougo/neocomplcache'
NeoBundle 'thinca/vim-ref'
NeoBundle 'thinca/vim-quickrun'
NeoBundle 'itchyny/calendar.vim'
NeoBundle 'tyru/open-browser.vim'
NeoBundle 'mattn/webapi-vim'
NeoBundle 'mhinz/vim-startify'
NeoBundle 'groovy.vim'
NeoBundle 'tpope/vim-fugitive'
filetype plugin indent on

" ��quickRun
let g:quickrun_config = {
\   "_" : {
\       "outputter" : "multi:buffer:quickfix",
\       "runner" : "vimproc",
\       "runner/vimproc/updatetime" : 40,
\   }
\}

" \       "outputter/buffer/split" : ":botright 8sp",

" ���C���T�[�g���[�h���̃n�C���C�g
let g:hi_insert = 'highlight StatusLine guifg=darkblue guibg=darkyellow gui=none ctermfg=blue ctermbg=yellow cterm=none'

if has('syntax') 
  augroup InsertHook    
    autocmd!
    autocmd InsertEnter * call s:StatusLine('Enter')
    autocmd InsertLeave * call s:StatusLine('Leave')  
  augroup END
endif

let s:slhlcmd = ''
function! s:StatusLine(mode)  
  if a:mode == 'Enter'
	  silent! let s:slhlcmd = 'highlight ' . s:GetHighlight('StatusLine')    
	  silent exec g:hi_insert  
  else    
	  highlight clear StatusLine    
	  silent exec s:slhlcmd  
  endif
endfunction

function! s:GetHighlight(hi)  
	redir => hl  
	exec 'highlight '.a:hi  
	redir END  
	let hl = substitute(hl, '[\r\n]', '', 'g')  
	let hl = substitute(hl, 'xxx', '', '')  
	return hl
endfunction

" �� �I���ӏ��̃R�s�[�A�E�N���b�N�Ńy�[�X�g
" copy paste GUI
set guioptions+=a
" copy paste CUI
set clipboard+=autoselect

nnoremap <RightMouse> "*p
inoremap <RightMouse> <C-r><C-o>

" �� startify
" �w�b�_�[�����ɕ\�����镶�����ݒ肷��(date�R�}���h�����s���ē��t��ݒ肵�Ă���)
"let g:startify_custom_header =
"  \ map(split(system('date'), '\n'), '"   ". v:val') + ['','']
" �f�t�H���g���ƁA�ŋߎg�����t�@�C���̐擪�͐����Ȃ̂ŁA�g�p����A���t�@�x�b�g���w��
" �悭�g���t�@�C�����u�b�N�}�[�N�Ƃ��ēo�^���Ă���
let g:startify_bookmarks = [
  \ '~/.vimrc',
  \ 'c:\oracle',
  \ 'y:\',
  \ ]

" ��calender-vim�ݒ�
let g:calendar_google_calendar = 1
let g:calendar_google_task = 1

" ��quickRun��groovy�ݒ�
let g:quickrun_config.groovy = {'command' : 'groovy', 'cmdopt' : ''}

" �� �J�����t�@�C���̃f�B���N�g�����J�����g�f�B���N�g���ɂ���
au   BufEnter *   execute ":lcd " . expand("%:p:h")
