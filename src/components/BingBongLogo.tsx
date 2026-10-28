import React from 'react';

interface Props {
  size?: number;
  className?: string;
  showText?: boolean;
  showTagline?: boolean;
  taglineText?: string;
}

export const BingBongLogo: React.FC<Props> = ({
  size = 40,
  className = '',
  showText = true,
  showTagline = false,
  taglineText = '',
}) => {
  return (
    <div className={`flex items-center gap-3 ${className}`}>
      <div
        style={{ width: size, height: size }}
        className="relative flex-shrink-0 rounded-2xl bg-gradient-to-br from-slate-900 to-slate-800 p-1 shadow-md border border-slate-700/50 flex items-center justify-center overflow-hidden"
      >
        <svg viewBox="0 0 100 100" className="w-full h-full">
          {/* Ping Pong Paddle Blade */}
          <circle cx="45" cy="45" r="28" fill="#FF5E00" />
          <circle cx="45" cy="45" r="26" fill="#FF7724" />
          {/* Handle */}
          <path d="M37 68 L24 88 L34 94 L47 74 Z" fill="#D97706" />
          <path d="M24 88 L34 94" stroke="#B45309" strokeWidth="2" />
          {/* Motion Trail */}
          <path d="M54 58 Q 66 74, 82 38" fill="none" stroke="#00ADB5" strokeWidth="4.5" strokeLinecap="round" />
          {/* Bouncing Ball */}
          <circle cx="82" cy="36" r="11" fill="#FFFFFF" />
          <circle cx="79" cy="33" r="3" fill="#FFF7ED" />
        </svg>
      </div>

      {showText && (
        <div className="flex flex-col">
          <div className="flex items-center gap-1 leading-none">
            <span className="text-xl font-black tracking-tight text-slate-100 dark:text-slate-100 text-slate-900">
              Bing
            </span>
            <span className="text-xl font-black tracking-tight text-brand-orange">
              bong
            </span>
            <span className="inline-block w-2 h-2 rounded-full bg-brand-turquoise mb-1"></span>
          </div>
          {showTagline && taglineText && (
            <span className="text-xs text-slate-500 dark:text-slate-400 font-medium mt-0.5">
              {taglineText}
            </span>
          )}
        </div>
      )}
    </div>
  );
};
