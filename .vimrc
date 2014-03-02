:cd ~/

" ■文字コード設定
if has('gui_running') && !has('unix')
  set encoding=utf-8
endif
set fileencodings=ucs-bom,iso-2022-jp,utf-8,cp932,euc-jp,default,latin
set termencoding=utf-8

" ■タブ
set tabstop=4

" ■ Windows gitのdiff設定
let $TERM='msys'

" ■perl-support
let g:Perl_Perltidy  = 'on'
let g:Perl_CodeSnippets  = $HOME . '/vim/bundles/perl-support.vim/perl-support/codesnippets/'

" コマンド履歴の設定
set viminfo='50,<2000,s100,:0,

" 開いたファイルのパスをカレントにする
"set autochdir

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

" ■Git
nnoremap [git]     <Nop>
nmap     <Leader>g [git]
nnoremap <silent>  [git]c :Gwrite<CR>:Gcommit -m 'hoge'<CR>:Git push<CR>
nnoremap <silent>  [git]r :Git pull origin master<CR>
nnoremap <silent>  [git]s :Gstatus<CR>

" ■ タブ操作
map <C-TAB> :tabnext<CR>

" ■SrcExpl
"自動でプレビューを表示する。
let g:SrcExpl_RefreshTime   = 1
"プレビューウインドウの高さ
let g:SrcExpl_WinHeight     = 9
"tagsは自動で作成する
let g:SrcExpl_UpdateTags    = 1
"マッピング
let g:SrcExpl_RefreshMapKey = "<Space>"
let g:SrcExpl_GoBackMapKey  = "<C-b>"
nmap <F8> ::TrinityToggleAll<CR>
nmap <F7> ::TrinityToggleSourceExplorer<CR>

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

" ■ag
nnoremap <F3> :Ag <C-R><C-W><CR>

" ■w3m
let g:w3m#external_browser = '"c:\Program Files\Internet Explorer\iexplore.exe"'
nnoremap [w3m]     <Nop>
nmap     <Leader>w [w3m]
nnoremap [w3m]w :W3m https://www.google.co.jp/search?as_q=<C-R><C-W><CR>
nnoremap [w3m]i :W3mShowExtenalBrowser<CR>
nnoremap [w3m]s <C-u>:W3m https://www.google.co.jp/search?as_q=
nnoremap [w3m]h :W3mHistory<CR>

" ■ Ctrl-spaceで補完
if has('unix') 
    inoremap <Nul> <C-n>
elseif has('win32')
    inoremap <C-Space> <C-n>
endif

" ■ vimrcのショートカット
nnoremap <Space>. :edit $MYVIMRC<CR>
nnoremap <Space>s. :source $MYVIMRC<CR>

" ■ perltidy
map <Space>p ! perl /usr/bin/perltidy.pl -pro=/oracle/home/.perltidyrc -st<CR>

" ■ neosnippet
" Plugin key-mappings.
imap <C-k>     <Plug>(neosnippet_expand_or_jump)
smap <C-k>     <Plug>(neosnippet_expand_or_jump)
xmap <C-k>     <Plug>(neosnippet_expand_target)

" SuperTab like snippets behavior.
imap <expr><TAB> neosnippet#expandable_or_jumpable() ?
\ "\<Plug>(neosnippet_expand_or_jump)"
\: pumvisible() ? "\<C-n>" : "\<TAB>"
smap <expr><TAB> neosnippet#expandable_or_jumpable() ?
\ "\<Plug>(neosnippet_expand_or_jump)"
\: "\<TAB>"

" For snippet_complete marker.
if has('conceal')
  set conceallevel=2 concealcursor=i
endif

" ■ neobundle
set nocompatible
if has('unix')
	if has('vim_starting')
		set runtimepath+=~/vim/plugins/neobundle.vim-master
    endif
elseif has('win32')
    if has('vim_starting')
    	set runtimepath+=~/vim/plugins/neobundle.vim-master/plugin/neobundle.vim
    endif
endif


call neobundle#rc(expand('~/vim/bundles/'))
NeoBundle 'Shougo/vimshell'
NeoBundle 'Shougo/unite.vim'
NeoBundle 'Shougo/neocomplcache'
NeoBundle 'Shougo/neosnippet'
NeoBundle 'Shougo/neosnippet-snippets'
NeoBundle 'Shougo/neomru.vim'
NeoBundle 'thinca/vim-ref'
NeoBundle 'thinca/vim-quickrun'
NeoBundle 'thinca/vim-singleton'
NeoBundle 'itchyny/calendar.vim'
NeoBundle 'tyru/open-browser.vim'
NeoBundle 'mattn/webapi-vim'
NeoBundle 'mhinz/vim-startify'
NeoBundle 'groovy.vim'
NeoBundle 'tpope/vim-fugitive'
NeoBundle 'vim-scripts/L9'
"NeoBundle 'vim-scripts/AutoComplPop'
"NeoBundle 'vim-scripts/ctags.vim' " uniteとぶつかる気がする
NeoBundle 'vim-scripts/SrcExpl'
NeoBundle 'vim-scripts/taglist.vim'
NeoBundle 'vim-scripts/Trinity'
"iNeoBundle 'vim-scripts/SingleCompile'
NeoBundle 'vim-jp/vital.vim'
NeoBundle 'kmnk/vim-unite-svn'
NeoBundle 'rking/ag.vim'
NeoBundle 'perl-support.vim'
NeoBundle 'derekwyatt/vim-scala'
NeoBundle 'yuratomo/w3m.vim'
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

" groovy設定
let g:quickrun_config.groovy = {'command' : 'groovy', 'cmdopt' : ''}

"let g:quickrun_config.scala = {'cmdopt' : '-Dfile.encoding=' . '&termencoding' , 'hook/output_encode/encoding' : '&termencoding'}

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
    let s:memofile = $HOME . "/memo.txt"
endif

" メモファイルを開く
nnoremap <Space>m :edit $HOME/memo.txt<CR>

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

" ■ vim常駐化
if has('win32') 
    call singleton#enable()
endif

