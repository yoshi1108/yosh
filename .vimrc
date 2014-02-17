if has('gui_running') && !has('unix')
  set encoding=utf-8
endif

set fileencodings=ucs-bom,iso-2022-jp,utf-8,cp932,euc-jp,default,latin

set viminfo='50,<2000,s100,:0,

" ■バックアップを/tmpにとる
set backupdir=/tmp

" ■ステータスラインにファイルエンコーディングと日時を表示する
function! GetStatusEx()
  let str = ''
  if &ft != ''
    let str = str . '[' . &ft . ']'
  endif
  if has('multi_byte')
    if &fenc != ''
      let str = str . '[' . &fenc . ']'
    elseif &enc != ''
      let str = str . '[' . &enc . ']'
    endif
  endif
  if &ff != ''
    let str = str . '[' . &ff . ']'
  endif
  return str
endfunction
function! g:Date()
    return strftime("%x(%a) %H:%M")
endfunction
set statusline=%<%f\ %m%r%h%w%=\%{g:Date()}\ \%{GetStatusEx()}\ \ %l,%c%V%8P

" ■unite
nnoremap    [unite]   <Nop>
nmap    <Leader>f [unite]
nnoremap [unite]u  :<C-u>Unite -no-split<Space>
nnoremap <silent> [unite]f :<C-u>Unite<Space>buffer<CR>
nnoremap <silent> [unite]b :<C-u>Unite<Space>bookmark<CR>
nnoremap <silent> [unite]m :<C-u>Unite<Space>file_mru<CR>
nnoremap <silent> [unite]r :<C-u>UniteWithBufferDir file<CR>
nnoremap <silent> ,vr :UniteResume<CR>

" ■ タブ操作
map <C-TAB> :tabnext<CR>

" ■ 画面でかくする
if has('win32') 
    nmap <Leader>s :SM 4<CR>
endif

" ■ 開いてるファイルのディレクトリをカレントディレクトリ
nmap <Leader>cd :cd %:p:h<CR>

"map <C-w> :tabclose<CR>
map <C-n> :tabnew<CR>

" ■インクリメンタルサーチ
set incsearch

" ■ Ctrl-spaceで補完
if has('unix') 
    inoremap <Nul> <C-n>
elseif has('win32')
    inoremap <C-Space> <C-n>
endif

" ■ perltidy
map ,pt ! perl c:\Perl\bin\perltidy.pl -st<CR>

" ■ neobundle
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
NeoBundle 'vim-scripts/L9'
NeoBundle 'vim-scripts/AutoComplPop'
NeoBundle 'vim-jp/vital.vim'
filetype plugin indent on

" ■quickRun
let g:quickrun_config = {
\   "_" : {
\       "outputter" : "multi:buffer:quickfix",
\       "runner" : "vimproc",
\       "runner/vimproc/updatetime" : 40,
\   }
\}

" \       "outputter/buffer/split" : ":botright 8sp",

" ■インサートモード時のハイライト
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

" ■ 選択箇所のコピー、右クリックでペースト
" copy paste GUI
set guioptions+=a
" copy paste CUI
set clipboard+=autoselect

nnoremap <RightMouse> "*p
inoremap <RightMouse> <C-r><C-o>

" ■ startify 
if has('unix') 
    let s:memofile = "~/memo.txt"
elseif has('win32')
    let s:memofile = "C:/Documents and Settings/yoshihara/memo.txt"
endif

let s:memo_list = []
for line in readfile(s:memofile)
	call add(s:memo_list, line)
endfor

let g:startify_custom_header = map([g:Date()], '"   ". v:val')
let g:startify_custom_footer = map(s:memo_list, '"   ". v:val')

let g:startify_bookmarks = [
  \ '~/.vimrc',
  \ 'c:\oracle',
  \ 'y:\',
  \ ]

" ■calender-vim設定
let g:calendar_google_calendar = 1
let g:calendar_google_task = 1

" ■quickRunのgroovy設定
let g:quickrun_config.groovy = {'command' : 'groovy', 'cmdopt' : ''}

" ■ 開いたファイルのディレクトリをカレントディレクトリにする
"au   BufEnter *   execute ":lcd " . expand("%:p:h")
